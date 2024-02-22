package com.chaincat.pay.constant;

/**
 * Redis键常量
 *
 * @author chenhaizhuang
 */
public interface RedisKeyConst {

    /**
     * 预支付锁
     */
    String PAY_PREPAY_LOCK = "pay:lock:prepay:{}:{}";

    /**
     * 订单锁
     */
    String PAY_ORDER_LOCK = "pay:lock:order:{}";

    /**
     * 退款执行锁
     */
    String PAY_REFUND_EXEC_LOCK = "pay:lock:refund:exec:{}";

    /**
     * 退款查询锁
     */
    String PAY_REFUND_QUERY_LOCK = "pay:lock:refund:query:{}";
}
