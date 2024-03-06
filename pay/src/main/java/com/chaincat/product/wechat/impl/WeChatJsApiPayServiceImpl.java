package com.chaincat.product.wechat.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.product.wechat.WeChatProperties;
import com.chaincat.product.wechat.WeChatUtils;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信JSAPI支付实现类
 *
 * @author chenhaizhuang
 */
@Service("weChatJsApi")
public class WeChatJsApiPayServiceImpl extends WeChatBasePayServiceImpl {

    private final JsapiServiceExtension jsapiService;

    public WeChatJsApiPayServiceImpl(WeChatProperties weChatProperties,
                                     PayTpProperties payTpProperties,
                                     NotificationParser notificationParser,
                                     RefundService refundService,
                                     JsapiServiceExtension jsapiService) {
        super(weChatProperties, payTpProperties, notificationParser, refundService);
        this.jsapiService = jsapiService;
    }

    @Override
    @SuppressWarnings("all")
    public String prepay(PayOrder payOrder) {
        Amount amount = new Amount();
        amount.setTotal(WeChatUtils.getAmountInt(payOrder.getOrderAmount()));
        Payer payer = new Payer();
        payer.setOpenid(payOrder.getProductOpenId());
        String beanName = payTpProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String payNotifyUrl = StrUtil.format(payTpProperties.getPayNotifyUrl(), beanName);

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(payOrder.getProductAppId());
        prepayRequest.setMchid(weChatProperties.getMerchantId());
        prepayRequest.setDescription(payOrder.getDescription());
        prepayRequest.setOutTradeNo(payOrder.getOrderId());
        prepayRequest.setTimeExpire(WeChatUtils.getTimeStr(payOrder.getExpireTime()));
        prepayRequest.setNotifyUrl(payNotifyUrl);
        prepayRequest.setAmount(amount);
        prepayRequest.setPayer(payer);

        try {
            PrepayWithRequestPaymentResponse prepayResponse = jsapiService.prepayWithRequestPayment(prepayRequest);
            Map<String, String> result = Map.of(
                    "appId", prepayResponse.getAppId(),
                    "timeStamp", prepayResponse.getTimeStamp(),
                    "nonceStr", prepayResponse.getNonceStr(),
                    "package", prepayResponse.getPackageVal(),
                    "signType", prepayResponse.getSignType(),
                    "paySign", prepayResponse.getPaySign()
            );
            return JSON.toJSONString(result);
        } catch (Exception e) {
            throw new BizException("微信JSAPI 预支付失败", e);
        }
    }

    @Override
    public void closePay(PayOrder payOrder) {
        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(weChatProperties.getMerchantId());
        closeOrderRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            jsapiService.closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new BizException("微信JSAPI 关闭订单失败", e);
        }
    }

    @Override
    public PayResult<Transaction> queryPay(PayOrder payOrder) {
        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(weChatProperties.getMerchantId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            Transaction transaction = jsapiService.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);
            return PayResult.of(payOrder.getOrderId(), transaction);
        } catch (Exception e) {
            throw new BizException("微信JSAPI 查询订单失败", e);
        }
    }
}
