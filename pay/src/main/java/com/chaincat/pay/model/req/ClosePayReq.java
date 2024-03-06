package com.chaincat.pay.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 关闭支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class ClosePayReq {

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
}
