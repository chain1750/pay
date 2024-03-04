package com.chaincat.product.alipay.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.product.ProductProperties;
import com.chaincat.product.alipay.AlipayFactoryConfig;
import com.chaincat.product.alipay.AlipayProperties;
import com.chaincat.product.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝JSAPI支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayJsApi")
public class AlipayJsApiPayServiceImpl extends AlipayBasePayServiceImpl {

    public AlipayJsApiPayServiceImpl(AlipayProperties alipayProperties,
                                     ProductProperties productProperties,
                                     AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, productProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        String beanName = productProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String payNotifyUrl = StrUtil.format(productProperties.getPayNotifyUrl(), beanName);
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setProductCode("JSAPI_PAY");
        model.setBuyerOpenId(payOrder.getProductOpenId());
        model.setOpAppId(payOrder.getProductAppId());
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        request.setNotifyUrl(payNotifyUrl);
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
