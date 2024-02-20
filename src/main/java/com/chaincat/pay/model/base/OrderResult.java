package com.chaincat.pay.model.base;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单结果
 *
 * @author chenhaizhuang
 */
@Data
public class OrderResult {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭
     */
    private String orderState;

    /**
     * 产品订单ID
     */
    private String productOrderId;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 业务名称，用于避免不同业务的业务ID重复，命名格式：模块_业务（大写字母）
     */
    private String bizName;

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作
     */
    private String bizTopic;

    /**
     * 业务附加信息，通知业务方时返回，若所需附加信息过长，建议存储在业务方
     */
    private String bizAttach;
}
