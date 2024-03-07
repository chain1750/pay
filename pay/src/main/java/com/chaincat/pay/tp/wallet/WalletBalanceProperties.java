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
}
