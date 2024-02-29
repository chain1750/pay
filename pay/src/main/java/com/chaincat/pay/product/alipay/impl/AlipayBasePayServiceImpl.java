package com.chaincat.pay.product.alipay.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.chaincat.pay.dao.entity.PayOrder;
import com.chaincat.pay.dao.entity.PayRefund;
import com.chaincat.pay.exception.BizException;
import com.chaincat.pay.model.base.PayResult;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.chaincat.pay.model.enums.RefundStateEnum;
import com.chaincat.pay.product.IPayService;
import com.chaincat.pay.product.ProductProperties;
import com.chaincat.pay.product.alipay.AlipayFactoryConfig;
import com.chaincat.pay.product.alipay.AlipayProperties;
import com.chaincat.pay.product.alipay.AlipayUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付宝支付实现类
 *
 * @author chenhaizhuang
 */
@Slf4j
public abstract class AlipayBasePayServiceImpl implements
        IPayService<AlipayTradeQueryResponse, AlipayTradeFastpayRefundQueryResponse> {

    protected final AlipayProperties alipayProperties;

    protected final ProductProperties productProperties;

    protected final AlipayFactoryConfig alipayFactoryConfig;

    public AlipayBasePayServiceImpl(AlipayProperties alipayProperties,
                                    ProductProperties productProperties,
                                    AlipayFactoryConfig alipayFactoryConfig) {
        this.alipayProperties = alipayProperties;
        this.productProperties = productProperties;
        this.alipayFactoryConfig = alipayFactoryConfig;
    }

    @Override
    public void closeOrder(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(payOrder.getOrderId());
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);

        AlipayTradeCloseResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new BizException("支付宝 关闭订单失败", e);
        }
        if (!response.isSuccess() && !"ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
            throw new BizException("支付宝 关闭订单失败：" + response.getSubMsg());
        }
    }

    @Override
    public PayResult<AlipayTradeQueryResponse> queryOrder(PayOrder payOrder) {
        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(payOrder.getOrderId());
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);

        AlipayTradeQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new BizException("支付宝 查询订单失败", e);
        }
        if (!response.isSuccess() && !"ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
            throw new BizException("支付宝 查询订单失败：" + response.getSubMsg());
        }
        if (!response.isSuccess() && "ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
            response = new AlipayTradeQueryResponse();
            response.setTradeStatus("WAIT_BUYER_PAY");
        }

        return PayResult.of(payOrder.getOrderId(), response);
    }

    @Override
    public PayResult<AlipayTradeQueryResponse> parsePayNotify(HttpServletRequest request) {
        Map<String, String> requestParam = AlipayUtils.getRequestParam(request.getParameterMap());
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV2(requestParam, alipayProperties.getPublicKey(), "utf-8", "RSA2");
        } catch (Exception e) {
            throw new BizException("支付宝 解析支付通知失败", e);
        }
        String sellerId = requestParam.get("seller_id");
        if (!signVerified || !alipayProperties.getSellerId().equals(sellerId)) {
            throw new BizException("支付宝 解析支付通知失败");
        }

        String outTradeNo = requestParam.get("out_trade_no");
        String tradeNo = requestParam.get("trade_no");
        String tradeStatus = requestParam.get("trade_status");
        String gmtPayment = requestParam.get("gmt_payment");
        AlipayTradeQueryResponse payResult = new AlipayTradeQueryResponse();
        payResult.setOutTradeNo(outTradeNo);
        payResult.setTradeNo(tradeNo);
        payResult.setTradeStatus(tradeStatus);
        payResult.setSendPayDate(AlipayUtils.getTime(gmtPayment));
        log.info("支付宝 支付通知结果：{}", payResult);
        return PayResult.of(outTradeNo, payResult);
    }

    @Override
    public boolean updateOrder(AlipayTradeQueryResponse alipayTradeQueryResponse, PayOrder payOrder) {
        String tradeStatus = alipayTradeQueryResponse.getTradeStatus();
        if ("WAIT_BUYER_PAY".equals(tradeStatus) && LocalDateTime.now().isAfter(payOrder.getExpireTime())) {
            log.info("订单已过期，执行关闭订单：{}", payOrder.getOrderId());
            closeOrder(payOrder);
            alipayTradeQueryResponse.setTradeStatus("TRADE_CLOSED");
        }

        boolean updated = false;
        tradeStatus = alipayTradeQueryResponse.getTradeStatus();
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            log.info("订单已支付：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.SUCCESS.name());
            payOrder.setPayTime(AlipayUtils.getTime(alipayTradeQueryResponse.getSendPayDate()));
            updated = true;
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            log.info("订单已关闭：{}", payOrder.getOrderId());
            payOrder.setOrderState(OrderStateEnum.CLOSED.name());
            updated = true;
        }
        payOrder.setProductOrderId(alipayTradeQueryResponse.getTradeNo());
        return updated;
    }

    @Override
    public void refund(PayRefund payRefund) {
        PayOrder payOrder = payRefund.getPayOrder();
        String beanName = productProperties.getEntities().get(payOrder.getProductName()).getBeanName();
        String refundNotifyUrl = StrUtil.format(productProperties.getRefundNotifyUrl(), beanName);

        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setRefundAmount(payRefund.getRefundAmount().toString());
        model.setRefundReason(payRefund.getRefundReason());
        model.setOutRequestNo(payRefund.getRefundId());
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        request.setNotifyUrl(refundNotifyUrl);

        AlipayTradeRefundResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new BizException("支付宝 退款失败", e);
        }
        if (!response.isSuccess()) {
            throw new BizException("支付宝 退款失败：" + response.getSubMsg());
        }
    }

    @Override
    public PayResult<AlipayTradeFastpayRefundQueryResponse> queryRefund(PayRefund payRefund) {
        PayOrder payOrder = payRefund.getPayOrder();

        AlipayClient alipayClient = alipayFactoryConfig.get(payOrder.getProductAppId());
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(payOrder.getOrderId());
        model.setOutRequestNo(payRefund.getRefundId());
        model.setQueryOptions(List.of("gmt_refund_pay"));
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);

        AlipayTradeFastpayRefundQueryResponse response;
        try {
            response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new BizException("支付宝 查询退款失败", e);
        }
        if (!response.isSuccess()) {
            throw new BizException("支付宝 查询退款失败：" + response.getSubMsg());
        }

        return PayResult.of(payRefund.getRefundId(), response);
    }

    @Override
    public PayResult<AlipayTradeFastpayRefundQueryResponse> parseRefundNotify(HttpServletRequest request) {
        Map<String, String> requestParam = AlipayUtils.getRequestParam(request.getParameterMap());
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV2(requestParam, alipayProperties.getPublicKey(), "utf-8", "RSA2");
        } catch (Exception e) {
            throw new BizException("支付宝 解析退款通知失败", e);
        }

        String sellerId = requestParam.get("seller_id");
        if (!signVerified || !alipayProperties.getSellerId().equals(sellerId)) {
            throw new BizException("支付宝 解析退款通知失败");
        }

        String outBizNo = requestParam.get("out_biz_no");
        String tradeStatus = requestParam.get("trade_status");
        String gmtRefund = requestParam.get("gmt_refund");
        String refundStatus = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_CLOSED".equals(tradeStatus)
                ? "REFUND_SUCCESS" : null;
        AlipayTradeFastpayRefundQueryResponse refundResult = new AlipayTradeFastpayRefundQueryResponse();
        refundResult.setOutRequestNo(outBizNo);
        refundResult.setRefundStatus(refundStatus);
        refundResult.setGmtRefundPay(AlipayUtils.getTimeWithMilli(gmtRefund));
        log.info("支付宝 退款通知结果：{}", refundResult);
        return PayResult.of(outBizNo, refundResult);
    }

    @Override
    public boolean updateRefund(AlipayTradeFastpayRefundQueryResponse alipayTradeFastpayRefundQueryResponse,
                                PayRefund payRefund) {
        boolean updated = false;

        String refundStatus = alipayTradeFastpayRefundQueryResponse.getRefundStatus();
        if ("REFUND_SUCCESS".equals(refundStatus)) {
            log.info("退款已完成：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.SUCCESS.name());
            payRefund.setRefundTime(AlipayUtils.getTime(alipayTradeFastpayRefundQueryResponse.getGmtRefundPay()));
            updated = true;
        } else if (LocalDateTime.now().isAfter(payRefund.getCreateTime().plusSeconds(10))) {
            log.info("退款失败：{}", payRefund.getRefundId());
            payRefund.setRefundState(RefundStateEnum.FAIL.name());
            payRefund.setRefundFailDesc(refundStatus);
            updated = true;
        }
        return updated;
    }
}
