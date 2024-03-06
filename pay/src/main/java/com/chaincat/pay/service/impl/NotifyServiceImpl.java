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
import com.chaincat.pay.service.NotifyService;
import com.chaincat.pay.strategy.IPayService;
import com.chaincat.pay.strategy.IPayStrategy;
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

    private final RocketMQTemplate rocketMQTemplate;

    private final IPayStrategy payStrategy;

    private final PayOrderMapper payOrderMapper;

    private final PayRefundMapper payRefundMapper;

    @Override
    public String handlePay(String payTpName, HttpServletRequest request) {
        // 支付第三方统一接口
        IPayService payService = payStrategy.get(payTpName);
        String notifyReturnData = payStrategy.getNotifyReturnData(payTpName);
        // 调用支付第三方统一接口的解析支付通知方法
        PayResult payResult = payService.parsePayNotify(request);

        String key = StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, payResult.getId());
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayOrder payOrder;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, payResult.getId()));
                if (OrderStateEnum.NOT_PAY != payOrder.getOrderState()) {
                    return notifyReturnData;
                }
            } while (!locked);

            // 调用支付第三方统一接口的更新支付订单方法（由于查询到的第三方数据不一致，所以有实现类设置更新数据）
            boolean updated = payService.updatePayOrder(payResult.getData(), payOrder);
            // 若存在数据更新，则执行更新
            if (updated) {
                payOrder.setUpdateTime(LocalDateTime.now());
                payOrderMapper.updateById(payOrder);
                log.info("支付通知-更新支付订单：{}", payOrder.getOrderId());

                // 支付通知更新支付订单之后，需将支付状态通知到业务方
                // 这里不保证消息发送成功，需要业务方通过轮询补偿的方式完成支付状态更新
                PayResp payResp = BeanUtil.copyProperties(payOrder, PayResp.class);
                String message = JSON.toJSONString(payResp);
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
    public String handleRefund(String payTpName, HttpServletRequest request) {
        // 支付第三方统一接口
        IPayService payService = payStrategy.get(payTpName);
        String notifyReturnData = payStrategy.getNotifyReturnData(payTpName);
        // 调用支付第三方统一接口的解析退款通知方法
        PayResult payResult = payService.parseRefundNotify(request);

        String key = StrUtil.format(RedisKeyConst.PAY_REFUND_QUERY_LOCK, payResult.getId());
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayRefund payRefund;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payRefund = payRefundMapper.selectOne(Wrappers.<PayRefund>lambdaQuery()
                        .eq(PayRefund::getRefundId, payResult.getId()));
                if (RefundStateEnum.PROCESSING != payRefund.getRefundState()) {
                    return notifyReturnData;
                }
            } while (!locked);

            // 调用支付第三方统一接口的更新支付退款方法（由于查询到的第三方数据不一致，所以有实现类设置更新数据）
            boolean updated = payService.updatePayRefund(payResult.getData(), payRefund);
            // 若存在数据更新，则执行更新
            if (updated) {
                payRefund.setUpdateTime(LocalDateTime.now());
                payRefundMapper.updateById(payRefund);
                log.info("退款通知-更新支付退款：{}", payRefund.getRefundId());
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
