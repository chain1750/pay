package com.chaincat.product.alipay.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.product.alipay.AlipayFactoryConfig;
import com.chaincat.product.alipay.AlipayProperties;
import com.chaincat.product.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝APP支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayApp")
public class AlipayAppPayServiceImpl extends AlipayBasePayServiceImpl {

    public AlipayAppPayServiceImpl(AlipayProperties alipayProperties,
                                   PayTpProperties payTpProperties,
                                   AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, payTpProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        String beanName = payTpProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String payNotifyUrl = StrUtil.format(payTpProperties.getPayNotifyUrl(), beanName);
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(payNotifyUrl);
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
