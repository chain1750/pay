package com.chaincat.pay.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 关闭订单请求
 *
 * @author chenhaizhuang
 */
@Data
public class OrderCloseReq {

    /**
     * 订单ID
     */
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
}
