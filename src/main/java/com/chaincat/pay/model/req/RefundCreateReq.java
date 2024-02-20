package com.chaincat.pay.model.req;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建退款请求
 *
 * @author chenhaizhuang
 */
@Data
public class RefundCreateReq {

    /**
     * 订单ID，关联pay_order
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    /**
     * 退款金额，不能大于订单金额，单位元
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额不能小于0.01")
    private BigDecimal refundAmount;

    /**
     * 退款原因，简略描述，详细描述在业务方存储
     */
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

    /**
     * 退款附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参
     */
    private String refundAttach;
}
