package com.chaincat.pay.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 查询支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class QueryPayReq {

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
}
