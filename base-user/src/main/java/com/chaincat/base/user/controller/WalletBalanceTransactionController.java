package com.chaincat.base.user.controller;

import com.chaincat.base.user.model.base.ApiResult;
import com.chaincat.base.user.model.req.WalletBalanceCloseReq;
import com.chaincat.base.user.model.req.WalletBalancePrepayReq;
import com.chaincat.base.user.model.req.WalletBalanceQueryReq;
import com.chaincat.base.user.model.req.WalletBalanceRefundReq;
import com.chaincat.base.user.model.resp.WalletBalancePrepayResp;
import com.chaincat.base.user.model.resp.WalletBalanceTransactionResp;
import com.chaincat.base.user.service.WalletBalanceTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 钱包余额交易接口
 *
 * @author chenhaizhuang
 */
@RestController
@RequestMapping("/wallet/balance/transaction")
@RequiredArgsConstructor
public class WalletBalanceTransactionController {

    private final WalletBalanceTransactionService walletBalanceTransactionService;

    /**
     * 预支付
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/prepay")
    public ApiResult<WalletBalancePrepayResp> prepay(@Valid @RequestBody WalletBalancePrepayReq req) {
        WalletBalancePrepayResp resp = walletBalanceTransactionService.prepay(req);
        return ApiResult.success(resp);
    }

    /**
     * 关闭交易
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/close")
    public ApiResult<Void> close(@Valid @RequestBody WalletBalanceCloseReq req) {
        walletBalanceTransactionService.close(req);
        return ApiResult.success();
    }

    /**
     * 查询交易
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/query")
    public ApiResult<WalletBalanceTransactionResp> query(@Valid @RequestBody WalletBalanceQueryReq req) {
        WalletBalanceTransactionResp resp = walletBalanceTransactionService.query(req);
        return ApiResult.success(resp);
    }

    /**
     * 退款
     *
     * @param req 请求
     * @return Result
     */
    @PostMapping("/refund")
    public ApiResult<Void> refund(@Valid @RequestBody WalletBalanceRefundReq req) {
        walletBalanceTransactionService.refund(req);
        return ApiResult.success();
    }
}
