package com.chaincat.pay.tp.wallet;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 钱包余额配置
 *
 * @author chenhaizhuang
 */
@Data
@Component
@ConfigurationProperties("pay.tp.wallet-balance")
public class WalletBalanceProperties {

    /**
     * 盐值
     */
    private String salt;

    /**
     * 预支付地址
     */
    private String prepayUrl;

    /**
     * 关闭支付地址
     */
    private String closePayUrl;

    /**
     * 查询支付地址
     */
    private String queryPayUrl;

    /**
     * 退款地址
     */
    private String refundUrl;

    /**
     * 查询退款地址
     */
    private String queryRefundUrl;
}
