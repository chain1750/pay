package com.chaincat.product.alipay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付宝配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay.channel.alipay")
public class AlipayProperties {

    /**
     * 支付宝支付接口的请求地址
     */
    private String serverUrl;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 商户ID
     */
    private String sellerId;

    /**
     * 应用
     */
    private Map<String, App> apps;

    @Data
    public static class App {

        /**
         * 应用私钥
         */
        private String privateKey;
    }
}
