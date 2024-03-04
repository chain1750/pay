package com.chaincat.product.wechat;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.app.AppServiceExtension;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信工厂配置
 *
 * @author chenhaizhuang
 */
@Configuration
@RequiredArgsConstructor
public class WeChatFactoryConfig {

    private final WeChatProperties weChatProperties;

    /**
     * RSA自动获取证书配置
     *
     * @return RSAAutoCertificateConfig
     */
    @Bean
    public RSAAutoCertificateConfig rsaAutoCertificateConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(weChatProperties.getMerchantId())
                .privateKeyFromPath(weChatProperties.getPrivateKeyPath())
                .merchantSerialNumber(weChatProperties.getSerialNumber())
                .apiV3Key(weChatProperties.getApiV3Key())
                .build();
    }

    /**
     * 微信支付通知解析器
     *
     * @param rsaAutoCertificateConfig RSAAutoCertificateConfig
     * @return NotificationParser
     */
    @Bean
    public NotificationParser notificationParser(RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        return new NotificationParser(rsaAutoCertificateConfig);
    }

    /**
     * 微信退款Service
     *
     * @param rsaAutoCertificateConfig RSAAutoCertificateConfig
     * @return RefundService
     */
    @Bean
    public RefundService refundService(RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        return new RefundService.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
    }

    /**
     * 微信APP支付Service
     *
     * @param rsaAutoCertificateConfig RSAAutoCertificateConfig
     * @return AppServiceExtension
     */
    @Bean
    public AppServiceExtension appServiceExtension(RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        return new AppServiceExtension.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
    }

    /**
     * 微信H5支付Service
     *
     * @param rsaAutoCertificateConfig RSAAutoCertificateConfig
     * @return H5Service
     */
    @Bean
    public H5Service h5Service(RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        return new H5Service.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
    }

    /**
     * 微信JSAPI支付Service
     *
     * @param rsaAutoCertificateConfig RSAAutoCertificateConfig
     * @return JsapiServiceExtension
     */
    @Bean
    public JsapiServiceExtension jsapiServiceExtension(RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        return new JsapiServiceExtension.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
    }

    /**
     * 微信Native支付Service
     *
     * @param rsaAutoCertificateConfig RSAAutoCertificateConfig
     * @return NativePayService
     */
    @Bean
    public NativePayService nativePayService(RSAAutoCertificateConfig rsaAutoCertificateConfig) {
        return new NativePayService.Builder()
                .config(rsaAutoCertificateConfig)
                .build();
    }
}
