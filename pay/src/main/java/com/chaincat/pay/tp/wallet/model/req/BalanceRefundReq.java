package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 余额退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class BalanceRefundReq {

    /**
     * 外部交易ID
     */
    private String outTradeId;

    /**
     * 外部退款ID
     */
    private String outRefundId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 通知地址
     */
    private String notifyUrl;
}
