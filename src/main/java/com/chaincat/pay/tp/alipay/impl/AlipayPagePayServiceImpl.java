package com.chaincat.pay.tp.alipay.impl;

import cn.hutool.core.lang.Assert;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.pay.tp.alipay.AlipayFactoryConfig;
import com.chaincat.pay.tp.alipay.AlipayProperties;
import com.chaincat.pay.tp.alipay.AlipayUtils;
import org.springframework.stereotype.Service;

/**
 * 支付宝电脑网站支付实现类
 *
 * @author chenhaizhuang
 */
@Service("alipayPage")
public class AlipayPagePayServiceImpl extends AlipayPayServiceImpl {

    public AlipayPagePayServiceImpl(AlipayProperties alipayProperties,
                                    PayTpProperties payTpProperties,
                                    AlipayFactoryConfig alipayFactoryConfig) {
        super(alipayProperties, payTpProperties, alipayFactoryConfig);
    }

    @Override
    public String prepay(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getPayTpAppId());
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setTotalAmount(payOrder.getOrderAmount().toString());
        model.setSubject(payOrder.getDescription());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeExpire(AlipayUtils.getTimeStr(payOrder.getExpireTime()));
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(payTpProperties.buildPayNotifyUrl(payOrder.getPayTpName()));
        request.setBizModel(model);

        AlipayTradePagePayResponse response;
        try {
            response = alipayClient.pageExecute(request);
        } catch (Exception e) {
            throw new BizException("支付宝电脑网站 预支付失败", e);
        }
        Assert.isTrue(response.isSuccess(), "支付宝电脑网站 预支付失败：" + response.getSubMsg());
        return response.getBody();
    }
}
