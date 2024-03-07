package com.chaincat.pay.tp.wallet.model.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包余额预支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalancePrepayResp {

    /**
     * 交易金额
     */
    private BigDecimal tradeAmount;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 签名
     */
    private String signature;
}
