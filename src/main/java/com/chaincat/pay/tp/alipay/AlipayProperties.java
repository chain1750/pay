package com.chaincat.pay.tp.alipay;

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
@ConfigurationProperties("pay.tp.alipay")
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
     * 应用ID -> 私钥
     */
    private Map<String, String> privateKeys;
}
