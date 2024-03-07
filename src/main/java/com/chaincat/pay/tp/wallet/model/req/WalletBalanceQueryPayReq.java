package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

/**
 * 钱包余额查询支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceQueryPayReq {

    /**
     * 外部交易ID
     */
    private String outTradeId;

    /**
     * 签名
     */
    private String sign;
}
