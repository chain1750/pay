package com.chaincat.base.user.service.impl;

import com.chaincat.base.user.model.req.WalletBalanceCloseReq;
import com.chaincat.base.user.model.req.WalletBalancePrepayReq;
import com.chaincat.base.user.model.req.WalletBalanceQueryReq;
import com.chaincat.base.user.model.req.WalletBalanceRefundReq;
import com.chaincat.base.user.model.resp.WalletBalancePrepayResp;
import com.chaincat.base.user.model.resp.WalletBalanceTransactionResp;
import com.chaincat.base.user.service.WalletBalanceTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 钱包余额交易Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletBalanceTransactionServiceImpl implements WalletBalanceTransactionService {

    @Override
    public WalletBalancePrepayResp prepay(WalletBalancePrepayReq req) {
        return null;
    }

    @Override
    public void close(WalletBalanceCloseReq req) {

    }

    @Override
    public WalletBalanceTransactionResp query(WalletBalanceQueryReq req) {
        return null;
    }

    @Override
    public void refund(WalletBalanceRefundReq req) {

    }
}
