package com.chaincat.pay.tp.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay.tp.wechat")
public class WeChatProperties {

    /**
     * 商户号
     */
    private String merchantId;

    /**
     * 私钥路径
     */
    private String privateKeyPath;

    /**
     * 证书序号
     */
    private String serialNumber;

    /**
     * API V3 Key
     */
    private String apiV3Key;
}
