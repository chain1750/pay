package com.chaincat.pay.product;

import com.chaincat.pay.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 统一支付接口获取策略
 *
 * @author chenhaizhuang
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class IPayStrategy {

    private final ApplicationContext applicationContext;

    private final ProductProperties productProperties;

    public <Order, Refund> IPayService<Order, Refund> get(String productName) {
        if (!productProperties.getEntities().containsKey(productName)) {
            throw new BizException("产品配置不存在");
        }
        String beanName = productProperties.getEntities().get(productName).getBeanName();
        return applicationContext.getBean(beanName, IPayService.class);
    }
}
