package com.chaincat.product.wechat.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.product.ProductProperties;
import com.chaincat.product.wechat.WeChatProperties;
import com.chaincat.product.wechat.WeChatUtils;
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
public class WeChatNativePayServiceImpl extends WeChatBasePayServiceImpl {

    private final NativePayService nativePayService;

    public WeChatNativePayServiceImpl(WeChatProperties weChatProperties,
                                      ProductProperties productProperties,
                                      NotificationParser notificationParser,
                                      RefundService refundService,
                                      NativePayService nativePayService) {
        super(weChatProperties, productProperties, notificationParser, refundService);
        this.nativePayService = nativePayService;
    }

    @Override
    @SuppressWarnings("all")
    public String prepay(PayOrder payOrder) {
        Amount amount = new Amount();
        amount.setTotal(WeChatUtils.getAmountInt(payOrder.getOrderAmount()));
        String beanName = productProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String payNotifyUrl = StrUtil.format(productProperties.getPayNotifyUrl(), beanName);

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(payOrder.getProductAppId());
        prepayRequest.setMchid(weChatProperties.getMerchantId());
        prepayRequest.setDescription(payOrder.getDescription());
        prepayRequest.setOutTradeNo(payOrder.getOrderId());
        prepayRequest.setTimeExpire(WeChatUtils.getTimeStr(payOrder.getExpireTime()));
        prepayRequest.setNotifyUrl(payNotifyUrl);
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
    public void closeOrder(PayOrder payOrder) {
        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(weChatProperties.getMerchantId());
        closeOrderRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            nativePayService.closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new BizException("微信Native 关闭订单失败", e);
        }
    }

    @Override
    public PayResult<Transaction> queryOrder(PayOrder payOrder) {
        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(weChatProperties.getMerchantId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);
            return PayResult.of(payOrder.getOrderId(), transaction);
        } catch (Exception e) {
            throw new BizException("微信Native 查询订单失败", e);
        }
    }
}
