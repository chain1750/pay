package com.chaincat.pay.model.resp;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.annotation.JSONField;
import com.chaincat.pay.model.enums.OrderStateEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class PayResp {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭
     */
    private OrderStateEnum orderState;

    /**
     * 支付第三方订单ID
     */
    private String payTpOrderId;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    @JSONField(format = DatePattern.NORM_DATETIME_MS_PATTERN)
    private LocalDateTime payTime;

    /**
     * 业务名称，用于避免不同业务的业务ID重复
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
