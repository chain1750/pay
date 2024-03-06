package com.chaincat.pay.controller;

import com.chaincat.pay.model.base.ApiResult;
import com.chaincat.pay.model.resp.PayResp;
import com.chaincat.pay.model.req.ClosePayReq;
import com.chaincat.pay.model.req.PrepayReq;
import com.chaincat.pay.model.req.QueryPayReq;
import com.chaincat.pay.model.req.RefundReq;
import com.chaincat.pay.model.resp.PrepayResp;
import com.chaincat.pay.model.resp.RefundResp;
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
     * 预支付
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/prepay")
    public ApiResult<PrepayResp> prepay(@Valid @RequestBody PrepayReq req) {
        PrepayResp resp = payService.prepay(req);
        return ApiResult.success(resp);
    }

    /**
     * 关闭支付，业务方需要取消支付，同时需要将支付第三方上的支付一同关闭
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/closePay")
    public ApiResult<Void> closePay(@Valid @RequestBody ClosePayReq req) {
        payService.closePay(req);
        return ApiResult.success();
    }

    /**
     * 查询支付，业务方自身需要轮询支付状态，通过该接口获取信息
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/queryPay")
    public ApiResult<PayResp> queryPay(@Valid @RequestBody QueryPayReq req) {
        PayResp payResp = payService.queryPay(req);
        return ApiResult.success(payResp);
    }

    /**
     * 退款
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/refund")
    public ApiResult<RefundResp> refund(@Valid @RequestBody RefundReq req) {
        RefundResp resp = payService.refund(req);
        return ApiResult.success(resp);
    }
}
