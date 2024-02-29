package com.chaincat.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
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
import com.chaincat.pay.product.ProductProperties;
import com.chaincat.pay.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 通知Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class NotifyServiceImpl implements NotifyService {

    private final RedissonClient redissonClient;

    private final IPayStrategy payStrategy;

    private final ProductProperties productProperties;

    private final PayOrderMapper payOrderMapper;

    private final PayRefundMapper payRefundMapper;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public String handlePay(String productName, HttpServletRequest request) {
        // 统一支付接口
        IPayService payService = payStrategy.get(productName);
        String notifyReturnData = productProperties.getEntities().get(productName).getNotifyReturnData();
        // 调用统一支付接口的解析支付通知方法
        PayResult payResult = payService.parsePayNotify(request);

        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, payResult.getId()));
        boolean locked = false;
        PayOrder payOrder;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, payResult.getId()));
                Assert.notNull(payOrder, "订单不存在");
                if (!OrderStateEnum.NOT_PAY.name().equals(payOrder.getOrderState())) {
                    log.info("订单非未支付状态，无需更新");
                    return notifyReturnData;
                }
            } while (!locked);

            // 解析获取支付渠道的订单结果之后，对系统订单进行更新
            boolean updated = payService.updateOrder(payResult.getData(), payOrder);
            if (updated) {
                payOrder.setUpdateTime(LocalDateTime.now());
                payOrderMapper.updateById(payOrder);
                log.info("支付通知-更新订单：{}", payOrder.getOrderId());

                // 支付通知更新订单之后，需将订单状态通知到业务方
                // 这里不保证消息发送成功，需要业务方通过轮询补偿的方式完成订单状态更新
                OrderResult orderResult = BeanUtil.copyProperties(payOrder, OrderResult.class);
                String message = JSON.toJSONString(orderResult);
                log.info("支付通知-消息发送: {}, {}", payOrder.getBizTopic(), message);
                Message<String> msg = MessageBuilder.withPayload(message).build();
                rocketMQTemplate.send(payOrder.getBizTopic(), msg);
                log.info("支付通知-消息发送成功, 消息id：{}", msg.getHeaders().get("id"));
            }
            return notifyReturnData;
        } catch (InterruptedException e) {
            throw new BizException("支付通知加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public String handleRefund(String productName, HttpServletRequest request) {
        // 统一支付接口
        IPayService payService = payStrategy.get(productName);
        String notifyReturnData = productProperties.getEntities().get(productName).getNotifyReturnData();
        // 调用统一支付接口的解析退款通知方法
        PayResult payResult = payService.parseRefundNotify(request);

        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_REFUND_QUERY_LOCK, payResult.getId()));
        boolean locked = false;
        PayRefund payRefund;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payRefund = payRefundMapper.selectOne(Wrappers.<PayRefund>lambdaQuery()
                        .eq(PayRefund::getRefundId, payResult.getId()));
                Assert.notNull(payRefund, "退款不存在");
                if (!RefundStateEnum.PROCESSING.name().equals(payRefund.getRefundState())) {
                    log.info("退款非处理中状态，无需更新");
                    return notifyReturnData;
                }
            } while (!locked);

            // 解析获取支付渠道的退款结果之后，对系统退款进行更新
            boolean updated = payService.updateRefund(payResult.getData(), payRefund);
            if (updated) {
                payRefund.setUpdateTime(LocalDateTime.now());
                payRefundMapper.updateById(payRefund);
                log.info("退款通知-更新退款：{}", payRefund.getRefundId());
            }
            return notifyReturnData;
        } catch (InterruptedException e) {
            throw new BizException("退款通知加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
