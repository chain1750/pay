package com.chaincat.pay.product.wechat.impl;

import cn.hutool.core.util.StrUtil;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.product.ProductProperties;
import com.chaincat.pay.product.wechat.WeChatProperties;
import com.chaincat.pay.product.wechat.WeChatUtils;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.app.AppServiceExtension;
import com.wechat.pay.java.service.payments.app.model.Amount;
import com.wechat.pay.java.service.payments.app.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.app.model.PrepayRequest;
import com.wechat.pay.java.service.payments.app.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.app.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信APP支付实现类
 *
 * @author chenhaizhuang
 */
@Service("weChatAppPayService")
public class WeChatAppPayServiceImpl extends WeChatBasePayServiceImpl {

    private final AppServiceExtension appService;

    public WeChatAppPayServiceImpl(WeChatProperties weChatProperties,
                                   ProductProperties productProperties,
                                   NotificationParser notificationParser,
                                   RefundService refundService,
                                   AppServiceExtension appService) {
        super(weChatProperties, productProperties, notificationParser, refundService);
        this.appService = appService;
    }

    @Override
    @SuppressWarnings("all")
    public Map<String, Object> prepay(PayOrder payOrder) {
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
            PrepayWithRequestPaymentResponse prepayResponse = appService.prepayWithRequestPayment(prepayRequest);
            return Map.of(
                    "appid", prepayResponse.getAppid(),
                    "partnerid", prepayResponse.getPartnerId(),
                    "prepayid", prepayResponse.getPrepayId(),
                    "package", prepayResponse.getPackageVal(),
                    "noncestr", prepayResponse.getNonceStr(),
                    "timestamp", prepayResponse.getTimestamp(),
                    "sign", prepayResponse.getSign()
            );
        } catch (Exception e) {
            throw new BizException("微信APP 预支付失败", e);
        }
    }

    @Override
    public void closeOrder(PayOrder payOrder) {
        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(weChatProperties.getMerchantId());
        closeOrderRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            appService.closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new BizException("微信APP 关闭订单失败", e);
        }
    }

    @Override
    public PayResult<Transaction> queryOrder(PayOrder payOrder) {
        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(weChatProperties.getMerchantId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            Transaction transaction = appService.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);
            return PayResult.of(payOrder.getOrderId(), transaction);
        } catch (Exception e) {
            throw new BizException("微信APP 查询订单失败", e);
        }
    }
}
