package com.chaincat.pay.service;

import com.chaincat.pay.model.base.OrderResult;
import com.chaincat.pay.model.req.OrderCloseReq;
import com.chaincat.pay.model.req.OrderCreateReq;
import com.chaincat.pay.model.req.OrderQueryReq;
import com.chaincat.pay.model.req.RefundCreateReq;
import com.chaincat.pay.model.resp.OrderCreateResp;
import com.chaincat.pay.model.resp.RefundCreateResp;

/**
 * 支付Service
 *
 * @author chenhaizhuang
 */
public interface PayService {

    /**
     * 订单预支付
     *
     * @param req 请求
     * @return OrderCreateResp
     */
    OrderCreateResp prepay(OrderCreateReq req);

    /**
     * 关闭订单
     *
     * @param req 请求
     */
    void close(OrderCloseReq req);

    /**
     * 查询订单
     *
     * @param req 请求
     * @return OrderResult
     */
    OrderResult query(OrderQueryReq req);

    /**
     * 退款
     *
     * @param req 请求
     * @return RefundCreateResp
     */
    RefundCreateResp refund(RefundCreateReq req);
}
