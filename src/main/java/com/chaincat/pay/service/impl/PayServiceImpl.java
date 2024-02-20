package com.chaincat.pay.service.impl;

import com.chaincat.pay.model.base.OrderResult;
import com.chaincat.pay.model.req.OrderCloseReq;
import com.chaincat.pay.model.req.OrderCreateReq;
import com.chaincat.pay.model.req.OrderQueryReq;
import com.chaincat.pay.model.req.RefundCreateReq;
import com.chaincat.pay.model.resp.OrderCreateResp;
import com.chaincat.pay.model.resp.RefundCreateResp;
import com.chaincat.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    @Override
    public OrderCreateResp prepay(OrderCreateReq req) {
        return null;
    }

    @Override
    public void close(OrderCloseReq req) {

    }

    @Override
    public OrderResult query(OrderQueryReq req) {
        return null;
    }

    @Override
    public RefundCreateResp refund(RefundCreateReq req) {
        return null;
    }
}
