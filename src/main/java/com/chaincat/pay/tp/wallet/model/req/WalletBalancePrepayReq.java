package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包余额预支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalancePrepayReq {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 外部交易ID
     */
    private String outTradeId;

    /**
     * 交易金额
     */
    private BigDecimal tradeAmount;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 通知地址
     */
    private String notifyUrl;

    /**
     * 签名
     */
    private String sign;
}
