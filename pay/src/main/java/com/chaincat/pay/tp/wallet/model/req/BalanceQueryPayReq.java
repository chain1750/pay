package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

/**
 * 余额查询支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class BalanceQueryPayReq {

    /**
     * 外部交易ID
     */
    private String outTradeId;
}
