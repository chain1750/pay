package com.chaincat.product.wechat.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.strategy.IPayService;
import com.chaincat.pay.strategy.PayTpProperties;
import com.chaincat.product.wechat.WeChatProperties;
import com.chaincat.product.wechat.WeChatUtils;
import com.wechat.pay.java.core.http.Constant;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 微信支付实现类
 *
 * @author chenhaizhuang
 */
@Slf4j
public abstract class WeChatBasePayServiceImpl implements IPayService<Transaction, Refund> {

    protected final WeChatProperties weChatProperties;

    protected final PayTpProperties payTpProperties;

    private final NotificationParser notificationParser;

    private final RefundService refundService;

    public WeChatBasePayServiceImpl(WeChatProperties weChatProperties,
                                    PayTpProperties payTpProperties,
                                    NotificationParser notificationParser,
                                    RefundService refundService) {
        this.weChatProperties = weChatProperties;
        this.payTpProperties = payTpProperties;
        this.notificationParser = notificationParser;
        this.refundService = refundService;
    }

    @Override
    public PayResult<Transaction> parsePayNotify(HttpServletRequest request) {
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader(Constant.WECHAT_PAY_SERIAL))
                    .nonce(request.getHeader(Constant.WECHAT_PAY_NONCE))
                    .signature(request.getHeader(Constant.WECHAT_PAY_SIGNATURE))
                    .timestamp(request.getHeader(Constant.WECHAT_PAY_TIMESTAMP))
                    .body(ServletUtil.getBody(request))
                    .build();
            log.info("微信 支付通知参数：{}", requestParam);
            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            log.info("微信 支付通知结果：{}", transaction);
            return PayResult.of(transaction.getOutTradeNo(), transaction);
        } catch (Exception e) {
            throw new BizException("微信 解析支付通知失败", e);
        }
    }

    @Override
    public boolean updatePayOrder(Transaction transaction, PayOrder payOrder) {
        // 如果支付渠道上的订单未支付，且已过期，关闭订单
        Transaction.TradeStateEnum tradeState = transaction.getTradeState();
        if (Transaction.TradeStateEnum.NOTPAY.equals(tradeState)
                && LocalDateTime.now().isAfter(payOrder.getExpireTime())) {
            log.info("订单已过期，执行关闭订单：{}", payOrder.getOrderId());
            closePay(payOrder);
            transaction.setTradeState(Transaction.TradeStateEnum.CLOSED);
        }
        // 更新已支付和已关闭的情况
        boolean updated = false;
        tradeState = transaction.getTradeState();
        if (Transaction.TradeStateEnum.SUCCESS.equals(tradeState)) {
            log.info("订单已支付：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.SUCCESS.name());
            payOrder.setPayTime(WeChatUtils.getTime(transaction.getSuccessTime()));
            updated = true;
        } else if (Transaction.TradeStateEnum.CLOSED.equals(tradeState)) {
            log.info("订单已关闭：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.CLOSED.name());
            updated = true;
        }
        payOrder.setProductOrderId(transaction.getTransactionId());
        return updated;
    }

    @Override
    public void refund(PayRefund payRefund) {
        PayOrder payOrder = payRefund.getPayOrder();
        String beanName = payTpProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String refundNotifyUrl = StrUtil.format(payTpProperties.getRefundNotifyUrl(), beanName);

        AmountReq amountReq = new AmountReq();
        amountReq.setRefund(WeChatUtils.getAmountLong(payRefund.getRefundAmount()));
        amountReq.setTotal(WeChatUtils.getAmountLong(payOrder.getOrderAmount()));
        amountReq.setCurrency("CNY");

        CreateRequest createRequest = new CreateRequest();
        createRequest.setOutTradeNo(payOrder.getOrderId());
        createRequest.setOutRefundNo(payRefund.getRefundId());
        createRequest.setReason(payRefund.getRefundReason());
        createRequest.setAmount(amountReq);
        createRequest.setNotifyUrl(refundNotifyUrl);

        try {
            refundService.create(createRequest);
        } catch (Exception e) {
            throw new BizException("微信 退款失败", e);
        }
    }

    @Override
    public PayResult<Refund> queryRefund(PayRefund payRefund) {
        QueryByOutRefundNoRequest queryByOutRefundNoRequest = new QueryByOutRefundNoRequest();
        queryByOutRefundNoRequest.setOutRefundNo(payRefund.getRefundId());

        try {
            Refund refund = refundService.queryByOutRefundNo(queryByOutRefundNoRequest);
            return PayResult.of(payRefund.getRefundId(), refund);
        } catch (Exception e) {
            throw new BizException("微信 查询退款失败", e);
        }
    }

    @Override
    public PayResult<Refund> parseRefundNotify(HttpServletRequest request) {
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader(Constant.WECHAT_PAY_SERIAL))
                    .nonce(request.getHeader(Constant.WECHAT_PAY_NONCE))
                    .signature(request.getHeader(Constant.WECHAT_PAY_SIGNATURE))
                    .timestamp(request.getHeader(Constant.WECHAT_PAY_TIMESTAMP))
                    .body(ServletUtil.getBody(request))
                    .build();
            log.info("微信 退款通知参数：{}", requestParam);
            RefundNotification refundNotification = notificationParser.parse(requestParam, RefundNotification.class);
            log.info("微信 退款通知结果：{}", refundNotification);
            Refund refund = new Refund();
            refund.setRefundId(refundNotification.getRefundId());
            refund.setOutRefundNo(refundNotification.getOutRefundNo());
            refund.setTransactionId(refundNotification.getTransactionId());
            refund.setOutTradeNo(refundNotification.getOutTradeNo());
            refund.setUserReceivedAccount(refundNotification.getUserReceivedAccount());
            refund.setSuccessTime(refundNotification.getSuccessTime());
            refund.setCreateTime(refundNotification.getCreateTime());
            refund.setPromotionDetail(refundNotification.getPromotionDetail());
            refund.setAmount(refundNotification.getAmount());
            refund.setChannel(refundNotification.getChannel());
            refund.setFundsAccount(refundNotification.getFundsAccount());
            refund.setStatus(refundNotification.getRefundStatus());

            return PayResult.of(refundNotification.getOutRefundNo(), refund);
        } catch (Exception e) {
            throw new BizException("微信 解析退款通知失败", e);
        }
    }

    @Override
    public boolean updatePayRefund(Refund refund, PayRefund payRefund) {
        // 更新已完成和失败的情况
        boolean updated = false;
        Status status = refund.getStatus();
        if (Status.SUCCESS.equals(status)) {
            log.info("退款已完成：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.SUCCESS.name());
            payRefund.setRefundTime(WeChatUtils.getTime(refund.getSuccessTime()));
            updated = true;
        } else if (Status.CLOSED.equals(status) || Status.ABNORMAL.equals(status)) {
            log.info("退款失败：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.FAIL.name());
            payRefund.setRefundFailDesc(status.name());
            updated = true;
        }
        payRefund.setProductRefundId(refund.getRefundId());
        return updated;
    }
}
