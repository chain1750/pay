package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

/**
 * 余额查询退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class BalanceQueryRefundReq {

    /**
     * 外部退款ID
     */
    private String outRefundId;
}
