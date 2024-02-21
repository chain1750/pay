package com.chaincat.pay.constant;

/**
 * Redis键常量
 *
 * @author chenhaizhuang
 */
public interface RedisKeyConst {

    String PAY_PREPAY_LOCK = "pay:lock:prepay:{}:{}";

    String PAY_ORDER_LOCK = "pay:lock:order:{}";

    String PAY_REFUND_EXEC_LOCK = "pay:lock:refund:exec:{}";

    String PAY_REFUND_QUERY_LOCK = "pay:lock:refund:query:{}";
}
