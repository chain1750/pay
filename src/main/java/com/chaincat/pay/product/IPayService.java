package com.chaincat.pay.product;

import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.model.base.PayResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一支付接口
 *
 * @author chenhaizhuang
 */
public interface IPayService<Order, Refund> {

    /**
     * 预支付
     *
     * @param payOrder 订单
     * @return Result
     */
    String prepay(PayOrder payOrder);

    /**
     * 关闭订单
     *
     * @param payOrder 订单
     */
    void closeOrder(PayOrder payOrder);

    /**
     * 查询订单
     *
     * @param payOrder 订单
     * @return Result
     */
    PayResult<Order> queryOrder(PayOrder payOrder);

    /**
     * 解析支付通知
     *
     * @param request 请求
     * @return Result
     */
    PayResult<Order> parsePayNotify(HttpServletRequest request);

    /**
     * 更新订单
     *
     * @param order    订单
     * @param payOrder 订单
     * @return Result
     */
    boolean updateOrder(Order order, PayOrder payOrder);

    /**
     * 退款
     *
     * @param payRefund 退款
     */
    void refund(PayRefund payRefund);

    /**
     * 查询退款
     *
     * @param payRefund 退款
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
     * 更新退款
     *
     * @param refund    退款
     * @param payRefund 退款
     * @return Result
     */
    boolean updateRefund(Refund refund, PayRefund payRefund);
}
