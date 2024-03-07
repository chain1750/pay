package com.chaincat.pay.service;

import com.chaincat.pay.model.resp.PayResp;
import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.RefundResp;

/**
 * 支付Service
 *
 * @author chenhaizhuang
 */
public interface PayService {

    /**
     * 预支付
     *
     * @param req 请求
     * @return Result
     */
    PrepayResp prepay(PrepayReq req);

    /**
     * 关闭支付
     *
     * @param req 请求
     */
    void closePay(ClosePayReq req);

    /**
     * 查询支付
     *
     * @param req 请求
     * @return Result
     */
    PayResp queryPay(QueryPayReq req);

    /**
     * 退款
     *
     * @param req 请求
     * @return Result
     */
    RefundResp refund(RefundReq req);
}
