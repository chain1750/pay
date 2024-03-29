package com.chaincat.pay.strategy;

import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 支付第三方统一接口获取策略
 *
 * @author chenhaizhuang
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class IPayStrategy {

    private final ApplicationContext applicationContext;

    private final PayTpProperties payTpProperties;

    public IPayService get(String payTpName) {
        Assert.isTrue(payTpProperties.getEntities().containsKey(payTpName), "不支持当前支付方式");
        String beanName = payTpProperties.getEntities().get(payTpName).getBeanName();
        return applicationContext.getBean(beanName, IPayService.class);
    }

    public String getNotifyReturnData(String payTpName) {
        Assert.isTrue(payTpProperties.getEntities().containsKey(payTpName), "不支持当前支付方式");
        return payTpProperties.getEntities().get(payTpName).getNotifyReturnData();
    }
}
