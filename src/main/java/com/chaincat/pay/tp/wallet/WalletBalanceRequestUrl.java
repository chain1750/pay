package com.chaincat.pay.tp.wallet;

/**
 * 钱包余额请求地址
 *
 * @author chenhaizhuang
 */
public interface WalletBalanceRequestUrl {

    /**
     * 预支付地址
     */
    String PREPAY_URL = "";

    /**
     * 关闭支付地址
     */
    String CLOSE_PAY_URL = "";

    /**
     * 查询支付地址
     */
    String QUERY_PAY_URL = "";

    /**
     * 退款地址
     */
    String REFUND_URL = "";

    /**
     * 查询退款地址
     */
    String QUERY_REFUND_URL = "";
}
