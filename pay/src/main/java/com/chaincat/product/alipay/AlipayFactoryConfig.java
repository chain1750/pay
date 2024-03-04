package com.chaincat.product.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.chaincat.pay.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付宝工厂配置
 *
 * @author chenhaizhuang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayFactoryConfig {

    private final Map<String, AlipayClient> alipayClientMap = new ConcurrentHashMap<>();

    private final AlipayProperties alipayProperties;

    @PostConstruct
    public void init() {
        Map<String, AlipayProperties.App> apps = alipayProperties.getApps();
        apps.forEach((key, value) -> {
            AlipayConfig alipayConfig = new AlipayConfig();
            alipayConfig.setServerUrl(alipayProperties.getServerUrl());
            alipayConfig.setAppId(key);
            alipayConfig.setPrivateKey(value.getPrivateKey());
            alipayConfig.setAlipayPublicKey(alipayProperties.getPublicKey());
            try {
                AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
                this.alipayClientMap.put(key, alipayClient);
            } catch (Exception e) {
                log.error("支付宝 获取支付宝客户失败", e);
            }
        });
    }

    public AlipayClient get(String appId) {
        AlipayClient alipayClient = this.alipayClientMap.get(appId);
        if (alipayClient == null) {
            throw new BizException("支付宝 获取支付宝客户失败");
        }
        return alipayClient;
    }
}
