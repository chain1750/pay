package com.chaincat.pay.tp.wechat.impl;

import com.alibaba.fastjson.JSON;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.wechat.WeChatProperties;
import com.chaincat.pay.tp.wechat.WeChatUtils;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信Native支付实现类
 *
 * @author chenhaizhuang
 */
@Service("weChatNative")
public class WeChatNativePayServiceImpl extends WeChatPayServiceImpl {

    private final NativePayService nativePayService;

    public WeChatNativePayServiceImpl(WeChatProperties weChatProperties,
                                      PayTpProperties payTpProperties,
                                      NotificationParser notificationParser,
                                      RefundService refundService,
                                      NativePayService nativePayService) {
        super(weChatProperties, payTpProperties, notificationParser, refundService);
        this.nativePayService = nativePayService;
    }

    @Override
    @SuppressWarnings("all")
    public String prepay(PayOrder payOrder) {
        Amount amount = new Amount();
        amount.setTotal(WeChatUtils.getAmountInt(payOrder.getOrderAmount()));

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(payOrder.getPayTpAppId());
        prepayRequest.setMchid(weChatProperties.getMerchantId());
        prepayRequest.setDescription(payOrder.getDescription());
        prepayRequest.setOutTradeNo(payOrder.getOrderId());
        prepayRequest.setTimeExpire(WeChatUtils.getTimeStr(payOrder.getExpireTime()));
        prepayRequest.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));
        prepayRequest.setAmount(amount);

        try {
            PrepayResponse prepayResponse = nativePayService.prepay(prepayRequest);
            Map<String, String> result = Map.of(
                    "code_url", prepayResponse.getCodeUrl()
            );
            return JSON.toJSONString(result);
        } catch (Exception e) {
            throw new BizException("微信Native 预支付失败", e);
        }
    }

    @Override
    public void closePay(PayOrder payOrder) {
        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(weChatProperties.getMerchantId());
        closeOrderRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            nativePayService.closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new BizException("微信Native 关闭支付失败", e);
        }
    }

    @Override
    public PayResult<Transaction> queryPay(PayOrder payOrder) {
        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(weChatProperties.getMerchantId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);
            return PayResult.of(payOrder.getOrderId(), transaction);
        } catch (Exception e) {
            throw new BizException("微信Native 查询支付失败", e);
        }
    }
}
