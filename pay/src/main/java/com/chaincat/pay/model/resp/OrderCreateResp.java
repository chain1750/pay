package com.chaincat.pay.model.resp;

import lombok.Data;

/**
 * 创建订单结果
 *
 * @author chenhaizhuang
 */
@Data
public class OrderCreateResp {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 预支付，由于业务方无需知晓预支付信息，且不同支付渠道之间预支付信息不同，所以采用json string返回
     */
    private String prepay;
}
