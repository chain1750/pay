package com.chaincat.base.user.model.req;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 钱包余额退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceRefundReq {

    /**
     * 外部交易ID
     */
    @NotBlank(message = "外部交易ID不能为空")
    private String outTradeId;

    /**
     * 外部退款ID
     */
    @NotBlank(message = "外部退款ID不能为空")
    private String outRefundId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额不能小于0.01")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

    /**
     * 通知地址
     */
    @NotBlank(message = "通知地址不能为空")
    private String notifyUrl;
}
