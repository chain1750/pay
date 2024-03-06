package com.chaincat.pay.tp.alipay.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.alipay.AlipayFactoryConfig;
import com.chaincat.pay.tp.alipay.AlipayProperties;
import com.chaincat.pay.tp.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝手机网站支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayWap")
public class AlipayWapPayServiceImpl extends AlipayPayServiceImpl {

    public AlipayWapPayServiceImpl(AlipayProperties alipayProperties,
                                   PayTpProperties payTpProperties,
                                   AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, payTpProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getPayTpAppId());
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setProductCode("QUICK_WAP_WAY");
        model.setSellerId(alipayProperties.getSellerId());
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));
        request.setBizModel(model);

        AlipayTradeWapPayResponse response;
        try {
            response = alipayClient.pageExecute(request);
        } catch (Exception e) {
            throw new BizException("支付宝手机网站 预支付失败", e);
        }
        if (!response.isSuccess()) {
            throw new BizException("支付宝手机网站 预支付失败：" + response.getSubMsg());
        }
        return response.getBody();
    }
}
