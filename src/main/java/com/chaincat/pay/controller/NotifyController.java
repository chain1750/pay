package com.chaincat.pay.controller;

import com.chaincat.pay.model.base.ApiResult;
import com.chaincat.pay.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 通知接口，实际返回数据在Result的data里，支付系统属于底层服务，不应该外部暴露接口
 *
 * @author chenhaizhuang
 */
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    /**
     * 处理支付通知
     *
     * @param productName 产品名称
     * @param request     请求
     * @return Result
     */
    @PostMapping("/pay/{productName}")
    public ApiResult<String> handlePay(@PathVariable String productName, HttpServletRequest request) {
        String result = notifyService.handlePay(productName, request);
        return ApiResult.success(result);
    }

    /**
     * 处理退款通知
     *
     * @param productName 产品名称
     * @param request     请求
     * @return Result
     */
    @PostMapping("/refund/{productName}")
    public ApiResult<String> handleRefund(@PathVariable String productName, HttpServletRequest request) {
        String result = notifyService.handleRefund(productName, request);
        return ApiResult.success(result);
    }
}
