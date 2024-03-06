package com.chaincat.pay.tp.wallet.impl;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.strategy.IPayService;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.wallet.model.enums.TradeStateEnum;
import com.chaincat.pay.tp.wallet.model.req.BalanceClosePayReq;
import com.chaincat.pay.tp.wallet.model.req.BalancePrepayReq;
import com.chaincat.pay.tp.wallet.model.req.BalanceQueryPayReq;
import com.chaincat.pay.tp.wallet.model.req.BalanceQueryRefundReq;
import com.chaincat.pay.tp.wallet.model.req.BalanceRefundReq;
import com.chaincat.pay.tp.wallet.model.resp.BalancePrepayResp;
import com.chaincat.pay.tp.wallet.model.resp.BalanceTradeResp;
import com.chaincat.pay.tp.wallet.service.BalanceTradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 余额支付实现类
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service("balance")
@RequiredArgsConstructor
public class BalancePayServiceImpl implements IPayService<BalanceTradeResp, BalanceTradeResp> {

    private final BalanceTradeService balanceTradeService;

    private final PayTpProperties payTpProperties;

    @Override
    public String prepay(PayOrder payOrder) {
        BalancePrepayReq req = new BalancePrepayReq();
        req.setUserId(payOrder.getUserId());
        req.setOutTradeId(payOrder.getOrderId());
        req.setTradeAmount(payOrder.getOrderAmount());
        req.setDescription(payOrder.getDescription());
        req.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));

        try {
            BalancePrepayResp resp = balanceTradeService.prepay(req);
            return JSON.toJSONString(resp);
        } catch (Exception e) {
            throw new BizException("余额 预支付失败", e);
        }
    }

    @Override
    public void closePay(PayOrder payOrder) {
        BalanceClosePayReq req = new BalanceClosePayReq();
        req.setOutTradeId(payOrder.getOrderId());

        try {
            balanceTradeService.closePay(req);
        } catch (Exception e) {
            throw new BizException("余额 关闭支付失败", e);
        }
    }

    @Override
    public PayResult<BalanceTradeResp> queryPay(PayOrder payOrder) {
        BalanceQueryPayReq req = new BalanceQueryPayReq();
        req.setOutTradeId(payOrder.getOrderId());

        try {
            BalanceTradeResp resp = balanceTradeService.queryPay(req);
            return PayResult.of(payOrder.getOrderId(), resp);
        } catch (Exception e) {
            throw new BizException("余额 查询支付失败", e);
        }
    }

    @Override
    public PayResult<BalanceTradeResp> parsePayNotify(HttpServletRequest request) {
        String body = ServletUtil.getBody(request);
        BalanceTradeResp resp = JSON.parseObject(body, BalanceTradeResp.class);
        return PayResult.of(resp.getOutTradeId(), resp);
    }

    @Override
    public boolean updatePayOrder(BalanceTradeResp balanceTradeResp, PayOrder payOrder) {
        // 如果未支付且已过期，则关闭支付
        TradeStateEnum tradeState = balanceTradeResp.getTradeState();
        if (TradeStateEnum.PROCESSING == tradeState && LocalDateTime.now().isAfter(payOrder.getExpireTime())) {
            log.info("支付已过期，执行关闭支付：{}", payOrder.getOrderId());
            closePay(payOrder);
            balanceTradeResp.setTradeState(TradeStateEnum.CLOSED);
        }
        // 更新已支付和已关闭的情况
        boolean updated = false;
        tradeState = balanceTradeResp.getTradeState();
        if (TradeStateEnum.SUCCESS == tradeState) {
            log.info("已支付完成：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.SUCCESS);
            payOrder.setPayTime(balanceTradeResp.getTradeTime());
            updated = true;
        } else if (TradeStateEnum.CLOSED == tradeState) {
            log.info("已关闭支付：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.CLOSED);
            updated = true;
        }
        payOrder.setPayTpOrderId(balanceTradeResp.getTradeId());
        return updated;
    }

    @Override
    public void refund(PayRefund payRefund) {
        PayOrder payOrder = payRefund.getPayOrder();

        BalanceRefundReq req = new BalanceRefundReq();
        req.setOutTradeId(payOrder.getOrderId());
        req.setOutRefundId(payRefund.getRefundId());
        req.setRefundAmount(payRefund.getRefundAmount());
        req.setRefundReason(payRefund.getRefundReason());
        req.setNotifyUrl(payTpProperties.buildRefundNotifyUrl(payOrder.getPayTpName()));

        try {
            balanceTradeService.refund(req);
        } catch (Exception e) {
            throw new BizException("余额 退款失败", e);
        }
    }

    @Override
    public PayResult<BalanceTradeResp> queryRefund(PayRefund payRefund) {
        BalanceQueryRefundReq req = new BalanceQueryRefundReq();
        req.setOutRefundId(payRefund.getRefundId());

        try {
            BalanceTradeResp resp = balanceTradeService.queryRefund(req);
            return PayResult.of(payRefund.getRefundId(), resp);
        } catch (Exception e) {
            throw new BizException("余额 查询退款失败", e);
        }
    }

    @Override
    public PayResult<BalanceTradeResp> parseRefundNotify(HttpServletRequest request) {
        String body = ServletUtil.getBody(request);
        BalanceTradeResp resp = JSON.parseObject(body, BalanceTradeResp.class);
        return PayResult.of(resp.getOutTradeId(), resp);
    }

    @Override
    public boolean updatePayRefund(BalanceTradeResp balanceTradeResp, PayRefund payRefund) {
        // 更新已完成和失败的情况
        boolean updated = false;
        TradeStateEnum tradeState = balanceTradeResp.getTradeState();
        if (TradeStateEnum.SUCCESS == tradeState) {
            log.info("已退款完成：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.SUCCESS);
            payRefund.setRefundTime(balanceTradeResp.getTradeTime());
            updated = true;
        } else if (TradeStateEnum.CLOSED == tradeState) {
            log.info("退款失败：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.FAIL);
            updated = true;
        }
        payRefund.setPayTpRefundId(balanceTradeResp.getTradeId());
        return updated;
    }
}
