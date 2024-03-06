package com.chaincat.pay.tp.alipay.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.alipay.AlipayFactoryConfig;
import com.chaincat.pay.tp.alipay.AlipayProperties;
import com.chaincat.pay.tp.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝JSAPI支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayJsApi")
public class AlipayJsApiPayServiceImpl extends AlipayPayServiceImpl {

    public AlipayJsApiPayServiceImpl(AlipayProperties alipayProperties,
                                     PayTpProperties payTpProperties,
                                     AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, payTpProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getPayTpAppId());
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setProductCode("JSAPI_PAY");
        model.setBuyerOpenId(payOrder.getPayTpOpenId());
        model.setOpAppId(payOrder.getPayTpAppId());
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));
        request.setBizModel(model);

        AlipayTradeCreateResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new BizException("支付宝小程序 预支付失败", e);
        }
        if (!response.isSuccess()) {
            throw new BizException("支付宝小程序 预支付失败：" + response.getSubMsg());
        }
        return response.getBody();
    }
}
