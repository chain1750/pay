package com.chaincat.pay.service;

/**
 * 任务Service
 *
 * @author chenhaizhuang
 */
public interface TaskService {

    /**
     * 处理订单
     */
    void handleOrder();

    /**
     * 处理退款
     */
    void handleRefund();
}
