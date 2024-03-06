package com.chaincat.product.wechat.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.product.wechat.WeChatProperties;
import com.chaincat.product.wechat.WeChatUtils;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.Amount;
import com.wechat.pay.java.service.payments.h5.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.h5.model.H5Info;
import com.wechat.pay.java.service.payments.h5.model.PrepayRequest;
import com.wechat.pay.java.service.payments.h5.model.PrepayResponse;
import com.wechat.pay.java.service.payments.h5.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.h5.model.SceneInfo;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 微信H5支付实现类
 *
 * @author chenhaizhuang
 */
@Service("weChatH5")
public class WeChatH5PayServiceImpl extends WeChatBasePayServiceImpl {

    private final H5Service h5Service;

    public WeChatH5PayServiceImpl(WeChatProperties weChatProperties,
                                  PayTpProperties payTpProperties,
                                  NotificationParser notificationParser,
                                  RefundService refundService,
                                  H5Service h5Service) {
        super(weChatProperties, payTpProperties, notificationParser, refundService);
        this.h5Service = h5Service;
    }

    @Override
    @SuppressWarnings("all")
    public String prepay(PayOrder payOrder) {
        Amount amount = new Amount();
        amount.setTotal(WeChatUtils.getAmountInt(payOrder.getOrderAmount()));
        H5Info h5Info = new H5Info();
        h5Info.setType("Wap");
        SceneInfo sceneInfo = new SceneInfo();
        sceneInfo.setPayerClientIp(payOrder.getUserIp());
        sceneInfo.setH5Info(h5Info);
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
        prepayRequest.setSceneInfo(sceneInfo);

        try {
            PrepayResponse prepayResponse = h5Service.prepay(prepayRequest);
            Map<String, String> result = Map.of(
                    "h5_url", prepayResponse.getH5Url()
            );
            return JSON.toJSONString(result);
        } catch (Exception e) {
            throw new BizException("微信H5 预支付失败", e);
        }
    }

    @Override
    public void closePay(PayOrder payOrder) {
        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(weChatProperties.getMerchantId());
        closeOrderRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            h5Service.closeOrder(closeOrderRequest);
        } catch (Exception e) {
            throw new BizException("微信H5 关闭订单失败", e);
        }
    }

    @Override
    public PayResult<Transaction> queryPay(PayOrder payOrder) {
        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(weChatProperties.getMerchantId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(payOrder.getOrderId());

        try {
            Transaction transaction = h5Service.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);
            return PayResult.of(payOrder.getOrderId(), transaction);
        } catch (Exception e) {
            throw new BizException("微信H5 查询订单失败", e);
        }
    }
}
