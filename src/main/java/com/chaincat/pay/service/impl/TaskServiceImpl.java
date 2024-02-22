package com.chaincat.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaincat.pay.constant.RedisKeyConst;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.dao.mapper.PayOrderMapper;
import com.chaincat.pay.dao.mapper.PayRefundMapper;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.OrderResult;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.product.IPayService;
import com.chaincat.pay.product.IPayStrategy;
import com.chaincat.pay.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 任务Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class TaskServiceImpl implements TaskService {

    private final RedissonClient redissonClient;

    private final IPayStrategy payStrategy;

    private final PayOrderMapper payOrderMapper;

    private final PayRefundMapper payRefundMapper;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void handleOrder() {
        List<PayOrder> payOrders = payOrderMapper.selectList(Wrappers.<PayOrder>lambdaQuery()
                .eq(PayOrder::getOrderState, OrderStateEnum.NOT_PAY.name()));
        log.info("获取未支付的订单，数量：{}", payOrders.size());
        payOrders.forEach(payOrder -> CompletableFuture.runAsync(() -> runHandleOrder(payOrder)));
    }

    private void runHandleOrder(PayOrder payOrder) {
        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, payOrder.getOrderId()));
        boolean locked = false;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, payOrder.getOrderId()));
                if (!OrderStateEnum.NOT_PAY.name().equals(payOrder.getOrderState())) {
                    return;
                }
            } while (!locked);

            // 统一支付接口
            IPayService payService = payStrategy.get(payOrder.getProductName());
            // 调用统一支付接口的查询订单方法
            PayResult payResult = payService.queryOrder(payOrder);
            // 查询到支付渠道的订单结果之后，对系统订单进行更新
            boolean updated = payService.updateOrder(payResult.getData(), payOrder);
            if (updated) {
                payOrder.setUpdateTime(LocalDateTime.now());
                payOrderMapper.updateById(payOrder);
                log.info("轮询订单-更新订单：{}", payOrder.getOrderId());

                // 轮询订单更新订单之后，需将订单状态通知到业务方
                // 这里不保证消息发送成功，需要业务方通过轮询补偿的方式完成订单状态更新
                OrderResult orderResult = BeanUtil.copyProperties(payOrder, OrderResult.class);
                String message = JSON.toJSONString(orderResult);
                log.info("轮询订单-消息发送: {}, {}", payOrder.getBizTopic(), message);
                Message<String> msg = MessageBuilder.withPayload(message).build();
                rocketMQTemplate.send(payOrder.getBizTopic(), msg);
                log.info("轮询订单-消息发送成功, 消息id：{}", msg.getHeaders().get("id"));
            }
        } catch (InterruptedException e) {
            throw new BizException("支付通知加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public void handleRefund() {
        List<PayRefund> payRefunds = payRefundMapper.selectList(Wrappers.<PayRefund>lambdaQuery()
                .eq(PayRefund::getRefundState, RefundStateEnum.PROCESSING.name()));
        log.info("获取处理中的退款，数量：{}", payRefunds.size());
        payRefunds.forEach(payRefund -> CompletableFuture.runAsync(() -> runHandleRefund(payRefund)));
    }

    private void runHandleRefund(PayRefund payRefund) {
        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_REFUND_QUERY_LOCK, payRefund.getRefundId()));
        boolean locked = false;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payRefund = payRefundMapper.selectOne(Wrappers.<PayRefund>lambdaQuery()
                        .eq(PayRefund::getRefundId, payRefund.getRefundId()));
                if (!RefundStateEnum.PROCESSING.name().equals(payRefund.getRefundState())) {
                    return;
                }
            } while (!locked);

            PayOrder payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                    .eq(PayOrder::getOrderId, payRefund.getOrderId()));
            // 统一支付接口
            IPayService payService = payStrategy.get(payOrder.getProductName());
            // 调用统一支付接口的查询退款方法
            PayResult payResult = payService.queryRefund(payRefund);
            // 解析获取支付渠道的退款结果之后，对系统退款进行更新
            boolean updated = payService.updateRefund(payResult.getData(), payRefund);
            if (updated) {
                payRefund.setUpdateTime(LocalDateTime.now());
                payRefundMapper.updateById(payRefund);
                log.info("轮询退款-更新退款：{}", payRefund.getRefundId());
            }
        } catch (InterruptedException e) {
            throw new BizException("支付通知加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
