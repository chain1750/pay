package com.chaincat.pay.tp.wallet.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.strategy.IPayService;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.wallet.WalletBalanceProperties;
import com.chaincat.pay.tp.wallet.WalletBalanceRequestUrl;
import com.chaincat.pay.tp.wallet.WalletBalanceUtils;
import com.chaincat.pay.tp.wallet.model.enums.TradeStateEnum;
import com.chaincat.pay.tp.wallet.model.req.WalletBalanceClosePayReq;
import com.chaincat.pay.tp.wallet.model.req.WalletBalancePrepayReq;
import com.chaincat.pay.tp.wallet.model.req.WalletBalanceQueryPayReq;
import com.chaincat.pay.tp.wallet.model.req.WalletBalanceQueryRefundReq;
import com.chaincat.pay.tp.wallet.model.req.WalletBalanceRefundReq;
import com.chaincat.pay.tp.wallet.model.resp.WalletBalancePrepayResp;
import com.chaincat.pay.tp.wallet.model.resp.WalletBalanceTradeResp;
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
@Service("walletBalance")
@RequiredArgsConstructor
public class WalletBalancePayServiceImpl implements IPayService<WalletBalanceTradeResp, WalletBalanceTradeResp> {

    private final WalletBalanceProperties walletBalanceProperties;

    private final PayTpProperties payTpProperties;

    @Override
    public String prepay(PayOrder payOrder) {
        WalletBalancePrepayReq req = new WalletBalancePrepayReq();
        req.setUserId(payOrder.getPayTpOpenId());
        req.setOutTradeId(payOrder.getOrderId());
        req.setTradeAmount(payOrder.getOrderAmount());
        req.setDescription(payOrder.getDescription());
        req.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));

        try {
            req.setSign(WalletBalanceUtils.getSign(BeanUtil.beanToMap(req), walletBalanceProperties.getSalt()));
            WalletBalancePrepayResp resp = (WalletBalancePrepayResp) WalletBalanceUtils
                    .sendRequest(WalletBalanceRequestUrl.PREPAY_URL, JSON.toJSONString(req));
            return JSON.toJSONString(resp);
        } catch (Exception e) {
            throw new BizException("钱包余额 预支付失败", e);
        }
    }

    @Override
    public void closePay(PayOrder payOrder) {
        WalletBalanceClosePayReq req = new WalletBalanceClosePayReq();
        req.setOutTradeId(payOrder.getOrderId());

        try {
            req.setSign(WalletBalanceUtils.getSign(BeanUtil.beanToMap(req), walletBalanceProperties.getSalt()));
            WalletBalanceUtils.sendRequest(WalletBalanceRequestUrl.CLOSE_PAY_URL, JSON.toJSONString(req));
        } catch (Exception e) {
            throw new BizException("钱包余额 关闭支付失败", e);
        }
    }

    @Override
    public PayResult<WalletBalanceTradeResp> queryPay(PayOrder payOrder) {
        WalletBalanceQueryPayReq req = new WalletBalanceQueryPayReq();
        req.setOutTradeId(payOrder.getOrderId());

        try {
            req.setSign(WalletBalanceUtils.getSign(BeanUtil.beanToMap(req), walletBalanceProperties.getSalt()));
            WalletBalanceTradeResp resp = (WalletBalanceTradeResp) WalletBalanceUtils
                    .sendRequest(WalletBalanceRequestUrl.QUERY_PAY_URL, JSON.toJSONString(req));
            return PayResult.of(payOrder.getOrderId(), resp);
        } catch (Exception e) {
            throw new BizException("钱包余额 查询支付失败", e);
        }
    }

    @Override
    public PayResult<WalletBalanceTradeResp> parsePayNotify(HttpServletRequest request) {
        String body = ServletUtil.getBody(request);
        JSONObject responseJsonObject = JSON.parseObject(body);
        String signFromRequest = responseJsonObject.getString("sign");
        Assert.isTrue(StrUtil.isNotEmpty(signFromRequest), "钱包余额 解析支付通知失败");
        responseJsonObject.remove("sign");
        String signFromGet = WalletBalanceUtils.getSign(responseJsonObject, walletBalanceProperties.getSalt());
        Assert.isTrue(signFromRequest.equals(signFromGet), "钱包余额 解析支付通知失败");
        WalletBalanceTradeResp resp = JSON.parseObject(body, WalletBalanceTradeResp.class);
        return PayResult.of(resp.getOutTradeId(), resp);
    }

    @Override
    public boolean updatePayOrder(WalletBalanceTradeResp walletBalanceTradeResp, PayOrder payOrder) {
        // 如果未支付且已过期，则关闭支付
        TradeStateEnum tradeState = walletBalanceTradeResp.getTradeState();
        if (TradeStateEnum.PROCESSING == tradeState && LocalDateTime.now().isAfter(payOrder.getExpireTime())) {
            log.info("支付已过期，执行关闭支付：{}", payOrder.getOrderId());
            closePay(payOrder);
            walletBalanceTradeResp.setTradeState(TradeStateEnum.CLOSED);
        }
        // 更新已支付和已关闭的情况
        boolean updated = false;
        tradeState = walletBalanceTradeResp.getTradeState();
        if (TradeStateEnum.SUCCESS == tradeState) {
            log.info("已支付完成：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.SUCCESS);
            payOrder.setPayTime(walletBalanceTradeResp.getTradeTime());
            updated = true;
        } else if (TradeStateEnum.CLOSED == tradeState) {
            log.info("已关闭支付：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.CLOSED);
            updated = true;
        }
        payOrder.setPayTpOrderId(walletBalanceTradeResp.getTradeId());
        return updated;
    }

    @Override
    public void refund(PayRefund payRefund) {
        PayOrder payOrder = payRefund.getPayOrder();

        WalletBalanceRefundReq req = new WalletBalanceRefundReq();
        req.setOutTradeId(payOrder.getOrderId());
        req.setOutRefundId(payRefund.getRefundId());
        req.setRefundAmount(payRefund.getRefundAmount());
        req.setRefundReason(payRefund.getRefundReason());
        req.setNotifyUrl(payTpProperties.buildRefundNotifyUrl(payOrder.getPayTpName()));

        try {
            req.setSign(WalletBalanceUtils.getSign(BeanUtil.beanToMap(req), walletBalanceProperties.getSalt()));
            WalletBalanceUtils.sendRequest(WalletBalanceRequestUrl.REFUND_URL, JSON.toJSONString(req));
        } catch (Exception e) {
            throw new BizException("余额 退款失败", e);
        }
    }

    @Override
    public PayResult<WalletBalanceTradeResp> queryRefund(PayRefund payRefund) {
        WalletBalanceQueryRefundReq req = new WalletBalanceQueryRefundReq();
        req.setOutRefundId(payRefund.getRefundId());

        try {
            req.setSign(WalletBalanceUtils.getSign(BeanUtil.beanToMap(req), walletBalanceProperties.getSalt()));
            WalletBalanceTradeResp resp = (WalletBalanceTradeResp) WalletBalanceUtils
                    .sendRequest(WalletBalanceRequestUrl.QUERY_REFUND_URL, JSON.toJSONString(req));
            return PayResult.of(payRefund.getRefundId(), resp);
        } catch (Exception e) {
            throw new BizException("钱包余额 查询退款失败", e);
        }
    }

    @Override
    public PayResult<WalletBalanceTradeResp> parseRefundNotify(HttpServletRequest request) {
        String body = ServletUtil.getBody(request);
        JSONObject responseJsonObject = JSON.parseObject(body);
        String signFromRequest = responseJsonObject.getString("sign");
        Assert.isTrue(StrUtil.isNotEmpty(signFromRequest), "钱包余额 解析退款通知失败");
        responseJsonObject.remove("sign");
        String signFromGet = WalletBalanceUtils.getSign(responseJsonObject, walletBalanceProperties.getSalt());
        Assert.isTrue(signFromRequest.equals(signFromGet), "钱包余额 解析退款通知失败");
        WalletBalanceTradeResp resp = JSON.parseObject(body, WalletBalanceTradeResp.class);
        return PayResult.of(resp.getOutTradeId(), resp);
    }

    @Override
    public boolean updatePayRefund(WalletBalanceTradeResp walletBalanceTradeResp, PayRefund payRefund) {
        // 更新已完成和失败的情况
        boolean updated = false;
        TradeStateEnum tradeState = walletBalanceTradeResp.getTradeState();
        if (TradeStateEnum.SUCCESS == tradeState) {
            log.info("已退款完成：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.SUCCESS);
            payRefund.setRefundTime(walletBalanceTradeResp.getTradeTime());
            updated = true;
        } else if (TradeStateEnum.CLOSED == tradeState) {
            log.info("退款失败：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.FAIL);
            updated = true;
        }
        payRefund.setPayTpRefundId(walletBalanceTradeResp.getTradeId());
        return updated;
    }
}
