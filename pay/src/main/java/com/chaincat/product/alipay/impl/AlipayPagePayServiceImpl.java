package com.chaincat.product.alipay.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.product.ProductProperties;
import com.chaincat.product.alipay.AlipayFactoryConfig;
import com.chaincat.product.alipay.AlipayProperties;
import com.chaincat.product.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝电脑网站支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayPage")
public class AlipayPagePayServiceImpl extends AlipayBasePayServiceImpl {

    public AlipayPagePayServiceImpl(AlipayProperties alipayProperties,
                                    ProductProperties productProperties,
                                    AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, productProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        String beanName = productProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String payNotifyUrl = StrUtil.format(productProperties.getPayNotifyUrl(), beanName);
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(payNotifyUrl);
        request.setBizModel(model);

        AlipayTradePagePayResponse response;
        try {
            response = alipayClient.pageExecute(request);
        } catch (Exception e) {
            throw new BizException("支付宝电脑网站 预支付失败", e);
        }
        if (!response.isSuccess()) {
            throw new BizException("支付宝电脑网站 预支付失败：" + response.getSubMsg());
        }
        return response.getBody();
    }
}
