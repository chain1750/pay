package com.chaincat.base.user.service;

import com.chaincat.base.user.model.req.WalletBalanceCloseReq;
import com.chaincat.base.user.model.req.WalletBalancePayReq;
import com.chaincat.base.user.model.req.WalletBalancePrepayReq;
import com.chaincat.base.user.model.req.WalletBalanceQueryReq;
import com.chaincat.base.user.model.req.WalletBalanceRefundReq;
import com.chaincat.base.user.model.resp.WalletBalancePrepayResp;
import com.chaincat.base.user.model.resp.WalletBalanceTransactionResp;

/**
 * 钱包余额交易Service
 *
 * @author chenhaizhuang
 */
public interface WalletBalanceTransactionService {

    /**
     * 预支付
     *
     * @param req 请求
     * @return Result
     */
    WalletBalancePrepayResp prepay(WalletBalancePrepayReq req);

    /**
     * 关闭交易
     *
     * @param req 请求
     */
    void close(WalletBalanceCloseReq req);

    /**
     * 查询交易
     *
     * @param req 请求
     * @return Result
     */
    WalletBalanceTransactionResp query(WalletBalanceQueryReq req);

    /**
     * 退款
     *
     * @param req 请求
     */
    void refund(WalletBalanceRefundReq req);

    /**
     * 交易支付
     *
     * @param req 请求
     */
    void pay(WalletBalancePayReq req);
}
