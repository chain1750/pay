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
import com.chaincat.pay.model.resp.PayResp;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.RefundResp;
import com.chaincat.pay.strategy.IPayService;
import com.chaincat.pay.strategy.IPayStrategy;
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
    public PrepayResp prepay(PrepayReq req) {
        // 防重复提交
        String key = StrUtil.format(RedisKeyConst.PAY_PREPAY_LOCK, req.getBizName(), req.getBizId());
        RLock lock = redissonClient.getLock(key);
        Assert.isTrue(lock.tryLock(), "请求频繁，请稍后再试");
        try {
            Assert.isTrue(LocalDateTime.now().isBefore(req.getExpireTime()), "过期时间必须大于当前时间");
            // 支付第三方统一接口
            IPayService payService = payStrategy.get(req.getPayTpName());
            // 创建支付订单
            LocalDateTime now = LocalDateTime.now();
            PayOrder payOrder = BeanUtil.copyProperties(req, PayOrder.class);
            payOrder.setOrderId(IdUtils.generate(IdUtils.PREFIX_ORDER, now));
            payOrder.setOrderState(OrderStateEnum.NOT_PAY);
            payOrder.setCreateTime(now);
            payOrder.setUpdateTime(now);
            payOrderMapper.insert(payOrder);
            log.info("创建支付订单：{}", JSON.toJSONString(payOrder));
            // 返回预支付结果
            PrepayResp resp = new PrepayResp();
            resp.setOrderId(payOrder.getOrderId());
            // 调用支付第三方统一接口的预支付方法获取预支付信息
            resp.setPrepayInfo(payService.prepay(payOrder));
            return resp;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePay(ClosePayReq req) {
        String key = StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, req.getOrderId());
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayOrder payOrder;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, req.getOrderId()));
                Assert.notNull(payOrder, "支付不存在");
                Assert.isTrue(OrderStateEnum.NOT_PAY == payOrder.getOrderState(), "已支付成功或已关闭支付，无法关闭");
            } while (!locked);
            // 支付第三方统一接口
            IPayService payService = payStrategy.get(payOrder.getPayTpName());
            // 更新的支付订单状态
            payOrder.setOrderState(OrderStateEnum.CLOSED);
            payOrder.setUpdateTime(LocalDateTime.now());
            payOrderMapper.updateById(payOrder);
            log.info("关闭支付订单：{}", payOrder.getOrderId());
            // 调用支付第三方统一接口的关闭支付方法
            payService.closePay(payOrder);
        } catch (InterruptedException e) {
            throw new BizException("关闭支付加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResp queryPay(QueryPayReq req) {
        String key = StrUtil.format(RedisKeyConst.PAY_ORDER_LOCK, req.getOrderId());
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        PayOrder payOrder;
        try {
            // 锁单循环，避免其它操作冲突
            do {
                locked = lock.tryLock(100L, TimeUnit.MILLISECONDS);
                payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                        .eq(PayOrder::getOrderId, req.getOrderId()));
                Assert.notNull(payOrder, "支付不存在");
                if (OrderStateEnum.NOT_PAY != payOrder.getOrderState()) {
                    return BeanUtil.copyProperties(payOrder, PayResp.class);
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
                log.info("查询支付-更新支付订单：{}", payOrder.getOrderId());
            }
            return BeanUtil.copyProperties(payOrder, PayResp.class);
        } catch (InterruptedException e) {
            throw new BizException("查询支付加锁失败", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResp refund(RefundReq req) {
        // 防重复提交
        String key = StrUtil.format(RedisKeyConst.PAY_REFUND_EXEC_LOCK, req.getOrderId());
        RLock lock = redissonClient.getLock(key);
        Assert.isTrue(lock.tryLock(), "请求频繁，请稍后再试");
        try {
            PayOrder payOrder = payOrderMapper.selectOne(Wrappers.<PayOrder>lambdaQuery()
                    .eq(PayOrder::getOrderId, req.getOrderId()));
            Assert.notNull(payOrder, "支付不存在");
            Assert.isTrue(OrderStateEnum.SUCCESS == payOrder.getOrderState(), "未支付成功，无法退款");
            // 支付第三方统一接口
            IPayService payService = payStrategy.get(payOrder.getPayTpName());
            // 创建支付退款
            LocalDateTime now = LocalDateTime.now();
            PayRefund payRefund = BeanUtil.copyProperties(req, PayRefund.class);
            payRefund.setRefundId(IdUtils.generate(IdUtils.PREFIX_REFUND, now));
            payRefund.setRefundState(RefundStateEnum.PROCESSING);
            payRefund.setCreateTime(now);
            payRefund.setUpdateTime(now);
            payRefundMapper.insert(payRefund);
            log.info("创建支付退款：{}", JSON.toJSONString(payRefund));
            // 调用支付第三方统一接口的退款方法
            payRefund.setPayOrder(payOrder);
            payService.refund(payRefund);
            // 返回参数
            RefundResp resp = new RefundResp();
            resp.setRefundId(payRefund.getRefundId());
            return resp;
        } finally {
            lock.unlock();
        }
    }
}
