package com.chaincat.pay.strategy;

import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.model.base.PayResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 支付第三方统一接口
 *
 * @author chenhaizhuang
 */
public interface IPayService<Pay, Refund> {

    /**
     * 预支付
     *
     * @param payOrder 支付订单
     * @return Result
     */
    String prepay(PayOrder payOrder);

    /**
     * 关闭支付
     *
     * @param payOrder 支付订单
     */
    void closePay(PayOrder payOrder);

    /**
     * 查询支付
     *
     * @param payOrder 支付订单
     * @return Result
     */
    PayResult<Pay> queryPay(PayOrder payOrder);

    /**
     * 解析支付通知
     *
     * @param request 请求
     * @return Result
     */
    PayResult<Pay> parsePayNotify(HttpServletRequest request);

    /**
     * 更新支付订单
     *
     * @param pay      第三方查询对象
     * @param payOrder 支付订单
     * @return Result
     */
    boolean updatePayOrder(Pay pay, PayOrder payOrder);

    /**
     * 退款
     *
     * @param payRefund 支付退款
     */
    void refund(PayRefund payRefund);

    /**
     * 查询退款
     *
     * @param payRefund 支付退款
     * @return Result
     */
    PayResult<Refund> queryRefund(PayRefund payRefund);

    /**
     * 解析退款通知
     *
     * @param request 请求
     * @return Result
     */
    PayResult<Refund> parseRefundNotify(HttpServletRequest request);

    /**
     * 更新支付退款
     *
     * @param refund    第三方查询对象
     * @param payRefund 支付退款
     * @return Result
     */
    boolean updatePayRefund(Refund refund, PayRefund payRefund);
}
