package com.chaincat.pay.strategy;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付第三方配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay.tp")
public class PayTpProperties {

    /**
     * 支付第三方名称 -> 实现
     */
    private Map<String, Impl> entities;

    /**
     * 支付通知地址
     */
    private String payNotifyUrl;

    /**
     * 退款通知地址
     */
    private String refundNotifyUrl;

    /**
     * 获取支付通知地址
     *
     * @param payTpName 支付第三方名称
     * @return String
     */
    public String buildPayNotifyUrl(String payTpName) {
        return StrUtil.format(payNotifyUrl, entities.get(payTpName).getBeanName());
    }

    /**
     * 获取退款通知地址
     *
     * @param payTpName 支付第三方名称
     * @return String
     */
    public String buildRefundNotifyUrl(String payTpName) {
        return StrUtil.format(refundNotifyUrl, entities.get(payTpName).getBeanName());
    }

    @Data
    public static class Impl {

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
