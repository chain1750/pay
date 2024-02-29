package com.chaincat.pay.controller;

import com.chaincat.pay.model.base.ApiResult;
import com.chaincat.pay.model.base.OrderResult;
import com.chaincat.pay.model.req.OrderCloseReq;
import com.chaincat.pay.model.req.OrderCreateReq;
import com.chaincat.pay.model.req.OrderQueryReq;
import com.chaincat.pay.model.req.RefundCreateReq;
import com.chaincat.pay.model.resp.OrderCreateResp;
import com.chaincat.pay.model.resp.RefundCreateResp;
import com.chaincat.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 支付接口
 *
 * @author chenhaizhuang
 */
@RestController
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 订单预支付
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/order/prepay")
    public ApiResult<OrderCreateResp> prepay(@Valid @RequestBody OrderCreateReq req) {
        OrderCreateResp resp = payService.prepay(req);
        return ApiResult.success(resp);
    }

    /**
     * 关闭订单，业务方需要取消订单，同时需要将支付渠道上的订单一同关闭
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/order/close")
    public ApiResult<Void> close(@Valid @RequestBody OrderCloseReq req) {
        payService.close(req);
        return ApiResult.success();
    }

    /**
     * 查询订单，业务方自身需要轮询订单状态，通过该接口获取
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/order/query")
    public ApiResult<OrderResult> query(@Valid @RequestBody OrderQueryReq req) {
        OrderResult orderResult = payService.query(req);
        return ApiResult.success(orderResult);
    }

    /**
     * 退款
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/refund")
    public ApiResult<RefundCreateResp> refund(@Valid @RequestBody RefundCreateReq req) {
        RefundCreateResp resp = payService.refund(req);
        return ApiResult.success(resp);
    }
}
