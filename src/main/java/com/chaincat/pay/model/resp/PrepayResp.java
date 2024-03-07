package com.chaincat.pay.model.resp;

import lombok.Data;

/**
 * 预支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class PrepayResp {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 预支付信息，由于业务方无需知晓预支付信息，且不同支付第三方之间预支付信息不同，所以采用json string返回
     */
    private String prepayInfo;
}
