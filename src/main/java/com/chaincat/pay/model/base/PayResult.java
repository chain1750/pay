package com.chaincat.pay.model.base;

import lombok.Data;

/**
 * 支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class PayResult<T> {

    /**
     * ID，对应orderId和refundId
     */
    private String id;

    /**
     * 数据，不同支付第三方的查询对象
     */
    private T data;

    public static <T> PayResult<T> of(String id, T data) {
        PayResult<T> payResult = new PayResult<>();
        payResult.setId(id);
        payResult.setData(data);
        return payResult;
    }
}
