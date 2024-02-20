package com.chaincat.pay.product;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 产品配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("product")
public class ProductProperties {

    /**
     * 产品名称 -> 产品
     */
    private Map<String, Product> entities;

    @Data
    public static class Product {

        /**
         * 实现类名称
         */
        private String beanName;

        /**
         * 通知返回数据
         */
        private String notifyReturnData;
    }
}
