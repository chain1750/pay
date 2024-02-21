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
import com.chaincat.pay.model.req.OrderCloseReq;
import com.chaincat.pay.model.req.OrderCreateReq;
import com.chaincat.pay.model.req.OrderQueryReq;
import com.chaincat.pay.model.req.RefundCreateReq;
import com.chaincat.pay.model.resp.OrderCreateResp;
import com.chaincat.pay.model.resp.RefundCreateResp;
import com.chaincat.pay.product.IPayService;
import com.chaincat.pay.product.IPayStrategy;
import com.chaincat.pay.service.PayService;
import com.chaincat.pay.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 支付Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class PayServiceImpl implements PayService {

    private final RedissonClient redissonClient;

    private final IPayStrategy payStrategy;

    private final PayOrderMapper payOrderMapper;

    private final PayRefundMapper payRefundMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResp prepay(OrderCreateReq req) {
        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_PREPAY_LOCK, req.getBizName(), req.getBizId()));
        Assert.isTrue(lock.tryLock(), "请求频繁，请稍后再试");
        try {
            Assert.isTrue(LocalDateTime.now().isBefore(req.getExpireTime()), "过期时间必须大于当前时间");
            IPayService payService = payStrategy.get(req.getProductName());

            LocalDateTime now = LocalDateTime.now();
            PayOrder payOrder = BeanUtil.copyProperties(req, PayOrder.class);
            payOrder.setOrderId(IdUtils.generate(IdUtils.PREFIX_ORDER, now));
            payOrder.setOrderState(OrderStateEnum.NOT_PAY.name());
            payOrder.setCreateTime(now);
            payOrder.setUpdateTime(now);
            payOrderMapper.insert(payOrder);
            log.info("创建订单：{}", JSON.toJSONString(payOrder));

            OrderCreateResp resp = new OrderCreateResp();
            resp.setOrderId(payOrder.getOrderId());
            resp.setPrepay(payService.prepay(payOrder));
            return resp;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(OrderCloseReq req) {
        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, req.getOrderId()));
        boolean locked = false;
        PayOrder payOrder;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, req.getOrderId()));
                Assert.notNull(payOrder, "订单不存在");
                if (!OrderStateEnum.NOT_PAY.name().equals(payOrder.getOrderState())) {
                    throw new BizException("订单非未支付状态，无法关闭");
                }
            } while (!locked);

            IPayService payService = payStrategy.get(payOrder.getProductName());

            payOrder.setOrderState(OrderStateEnum.CLOSED.name());
            payOrder.setUpdateTime(LocalDateTime.now());
            payOrderMapper.updateById(payOrder);
            log.info("关闭订单：{}", payOrder.getOrderId());

            payService.closeOrder(payOrder);
        } catch (InterruptedException e) {
            throw new BizException("关闭订单加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResult query(OrderQueryReq req) {
        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, req.getOrderId()));
        boolean locked = false;
        PayOrder payOrder;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, req.getOrderId()));
                Assert.notNull(payOrder, "订单不存在");
                if (!OrderStateEnum.NOT_PAY.name().equals(payOrder.getOrderState())) {
                    log.info("订单非未支付状态，可直接返回");
                    return BeanUtil.copyProperties(payOrder, OrderResult.class);
                }
            } while (!locked);

            IPayService payService = payStrategy.get(payOrder.getProductName());
            PayResult payResult = payService.queryOrder(payOrder);
            boolean updated = payService.updateOrder(payResult.getData(), payOrder);
            if (updated) {
                payOrder.setUpdateTime(LocalDateTime.now());
                payOrderMapper.updateById(payOrder);
                log.info("更新订单：{}", payOrder.getOrderId());
            }
            return BeanUtil.copyProperties(payOrder, OrderResult.class);
        } catch (InterruptedException e) {
            throw new BizException("关闭订单加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundCreateResp refund(RefundCreateReq req) {
        RLock lock = redissonClient.getLock(StrUtil.format(RedisKeyConst.PAY_REFUND_EXEC_LOCK, req.getOrderId()));
        Assert.isTrue(lock.tryLock(), "请求频繁，请稍后再试");
        try {
            PayOrder payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                    .eq(PayOrder::getOrderId, req.getOrderId()));
            Assert.notNull(payOrder, "订单不存在");
            if (!OrderStateEnum.SUCCESS.name().equals(payOrder.getOrderState())) {
                throw new BizException("订单未支付，不可退款");
            }
            IPayService payService = payStrategy.get(payOrder.getProductName());

            LocalDateTime now = LocalDateTime.now();
            PayRefund payRefund = BeanUtil.copyProperties(req, PayRefund.class);
            payRefund.setRefundId(IdUtils.generate(IdUtils.PREFIX_REFUND, now));
            payRefund.setRefundState(RefundStateEnum.PROCESSING.name());
            payRefund.setCreateTime(now);
            payRefund.setUpdateTime(now);
            payRefundMapper.insert(payRefund);
            log.info("创建退款：{}", JSON.toJSONString(payRefund));

            payRefund.setPayOrder(payOrder);
            payService.refund(payRefund);

            RefundCreateResp resp = new RefundCreateResp();
            resp.setRefundId(payRefund.getRefundId());
            return resp;
        } finally {
            lock.unlock();
        }
    }
}
