package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

/**
 * 余额关闭支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class BalanceClosePayReq {

    /**
     * 外部交易ID
     */
    private String outTradeId;
}
