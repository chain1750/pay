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
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.model.resp.PayResp;
import com.chaincat.pay.service.TaskService;
import com.chaincat.pay.strategy.IPayService;
import com.chaincat.pay.strategy.IPayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 任务Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class TaskServiceImpl implements TaskService {

    private final RedissonClient redissonClient;

    private final RocketMQTemplate rocketMQTemplate;

    private final IPayStrategy payStrategy;

    private final PayOrderMapper payOrderMapper;

    private final PayRefundMapper payRefundMapper;

    private final Executor commonExecutor;

    public TaskServiceImpl(RedissonClient redissonClient,
                           RocketMQTemplate rocketMQTemplate,
                           IPayStrategy payStrategy,
                           PayOrderMapper payOrderMapper,
                           PayRefundMapper payRefundMapper,
                           @Qualifier("commonExecutor") Executor commonExecutor) {
        this.redissonClient = redissonClient;
        this.rocketMQTemplate = rocketMQTemplate;
        this.payStrategy = payStrategy;
        this.payOrderMapper = payOrderMapper;
        this.payRefundMapper = payRefundMapper;
        this.commonExecutor = commonExecutor;
    }

    @Override
    public void handleOrder() {
        List<PayOrder> payOrders = payOrderMapper.selectList(Wrappers.<PayOrder>lambdaQuery()
                .eq(PayOrder::getOrderState, OrderStateEnum.NOT_PAY));
        log.info("获取未支付的订单，数量：{}", payOrders.size());
        payOrders.forEach(payOrder -> CompletableFuture.runAsync(() -> runHandleOrder(payOrder), commonExecutor));
    }

    private void runHandleOrder(PayOrder payOrder) {
        String key = StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, payOrder.getOrderId());
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, payOrder.getOrderId()));
                if (OrderStateEnum.NOT_PAY != payOrder.getOrderState()) {
                    return;
                }
            } while (!locked);

            // 支付第三方统一接口
            IPayService payService = payStrategy.get(payOrder.getPayTpName());
            // 调用支付第三方统一接口的查询支付方法
            PayResult payResult = payService.queryPay(payOrder);
            // 调用支付第三方统一接口的更新支付订单方法（由于查询到的第三方数据不一致，所以有实现类设置更新数据）
            boolean updated = payService.updatePayOrder(payResult.getData(), payOrder);
            // 若存在数据更新，则执行更新
            if (updated) {
                payOrder.setUpdateTime(LocalDateTime.now());
                payOrderMapper.updateById(payOrder);
                log.info("轮询支付订单-更新支付订单：{}", payOrder.getOrderId());

                // 轮询支付订单更新支付订单之后，需将支付状态通知到业务方
                // 这里不保证消息发送成功，需要业务方通过轮询补偿的方式完成支付状态更新
                PayResp payResp = BeanUtil.copyProperties(payOrder, PayResp.class);
                String message = JSON.toJSONString(payResp);
                log.info("轮询支付订单-消息发送: {}, {}", payOrder.getBizTopic(), message);
                Message<String> msg = MessageBuilder.withPayload(message).build();
                rocketMQTemplate.send(payOrder.getBizTopic(), msg);
                log.info("轮询支付订单-消息发送成功, 消息id：{}", msg.getHeaders().get("id"));
            }
        } catch (InterruptedException e) {
            throw new BizException("轮询支付订单加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public void handleRefund() {
        List<PayRefund> payRefunds = payRefundMapper.selectList(Wrappers.<PayRefund>lambdaQuery()
                .eq(PayRefund::getRefundState, RefundStateEnum.PROCESSING));
        log.info("获取处理中的退款，数量：{}", payRefunds.size());
        payRefunds.forEach(payRefund -> CompletableFuture.runAsync(() -> runHandleRefund(payRefund), commonExecutor));
    }

    private void runHandleRefund(PayRefund payRefund) {
        String key = StrUtil.format(RedisKeyConst.PAY_REFUND_QUERY_LOCK, payRefund.getRefundId());
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payRefund = payRefundMapper.selectOne(Wrappers.<PayRefund>lambdaQuery()
                        .eq(PayRefund::getRefundId, payRefund.getRefundId()));
                if (RefundStateEnum.PROCESSING != payRefund.getRefundState()) {
                    return;
                }
            } while (!locked);

            PayOrder payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                    .eq(PayOrder::getOrderId, payRefund.getOrderId()));
            // 支付第三方统一接口
            IPayService payService = payStrategy.get(payOrder.getPayTpName());
            // 调用支付第三方统一接口的查询退款方法
            PayResult payResult = payService.queryRefund(payRefund);
            // 调用支付第三方统一接口的更新支付退款方法（由于查询到的第三方数据不一致，所以有实现类设置更新数据）
            boolean updated = payService.updatePayRefund(payResult.getData(), payRefund);
            // 若存在数据更新，则执行更新
            if (updated) {
                payRefund.setUpdateTime(LocalDateTime.now());
                payRefundMapper.updateById(payRefund);
                log.info("轮询支付退款-更新支付退款：{}", payRefund.getRefundId());
            }
        } catch (InterruptedException e) {
            throw new BizException("轮询支付退款加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
