package com.chaincat.pay.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 通知Service
 *
 * @author chenhaizhuang
 */
public interface NotifyService {

    /**
     * 处理支付通知
     *
     * @param payTpName 支付第三方名称
     * @param request   请求
     * @return Result
     */
    String handlePay(String payTpName, HttpServletRequest request);

    /**
     * 处理退款通知
     *
     * @param payTpName 支付第三方名称
     * @param request   请求
     * @return Result
     */
    String handleRefund(String payTpName, HttpServletRequest request);
}
