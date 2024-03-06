package com.chaincat.pay.tp.wallet.service;

import com.chaincat.pay.tp.wallet.model.req.BalanceClosePayReq;
import com.chaincat.pay.tp.wallet.model.req.BalancePrepayReq;
import com.chaincat.pay.tp.wallet.model.req.BalanceQueryPayReq;
import com.chaincat.pay.tp.wallet.model.req.BalanceQueryRefundReq;
import com.chaincat.pay.tp.wallet.model.req.BalanceRefundReq;
import com.chaincat.pay.tp.wallet.model.resp.BalancePrepayResp;
import com.chaincat.pay.tp.wallet.model.resp.BalanceTradeResp;

/**
 * 余额交易实现
 *
 * @author chenhaizhuang
 */
public interface BalanceTradeService {

    /**
     * 预支付
     *
     * @param req 请求
     * @return Result
     */
    BalancePrepayResp prepay(BalancePrepayReq req);

    /**
     * 关闭支付
     *
     * @param req 请求
     */
    void closePay(BalanceClosePayReq req);

    /**
     * 查询支付
     *
     * @param req 请求
     * @return Result
     */
    BalanceTradeResp queryPay(BalanceQueryPayReq req);

    /**
     * 退款
     *
     * @param req 请求
     */
    void refund(BalanceRefundReq req);

    /**
     * 查询退款
     *
     * @param req 请求
     * @return Result
     */
    BalanceTradeResp queryRefund(BalanceQueryRefundReq req);
}
