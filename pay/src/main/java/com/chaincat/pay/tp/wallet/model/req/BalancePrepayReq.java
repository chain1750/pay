package com.chaincat.pay.tp.wallet.model.req;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 余额预支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class BalancePrepayReq {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 外部交易ID
     */
    @NotBlank(message = "外部交易ID")
    private String outTradeId;

    /**
     * 交易金额
     */
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额不能小于0.01")
    private BigDecimal tradeAmount;

    /**
     * 交易描述
     */
    @NotBlank(message = "交易描述不能为空")
    private String description;

    /**
     * 通知地址
     */
    @NotBlank(message = "通知地址不能为空")
    private String notifyUrl;
}
