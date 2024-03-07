package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

/**
 * 钱包余额查询退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceQueryRefundReq {

    /**
     * 外部退款ID
     */
    private String outRefundId;

    /**
     * 签名
     */
    private String sign;
}
