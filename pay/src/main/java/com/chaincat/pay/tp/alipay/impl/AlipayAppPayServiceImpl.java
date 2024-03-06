package com.chaincat.pay.tp.alipay.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.alipay.AlipayFactoryConfig;
import com.chaincat.pay.tp.alipay.AlipayProperties;
import com.chaincat.pay.tp.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝APP支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayApp")
public class AlipayAppPayServiceImpl extends AlipayPayServiceImpl {

    public AlipayAppPayServiceImpl(AlipayProperties alipayProperties,
                                   PayTpProperties payTpProperties,
                                   AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, payTpProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getPayTpAppId());
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));
        request.setBizModel(model);

        AlipayTradeAppPayResponse response;
        try {
            response = alipayClient.sdkExecute(request);
        } catch (Exception e) {
            throw new BizException("支付宝APP 预支付失败", e);
        }
        if (!response.isSuccess()) {
            throw new BizException("支付宝APP 预支付失败：" + response.getSubMsg());
        }
        return response.getBody();
    }
}
