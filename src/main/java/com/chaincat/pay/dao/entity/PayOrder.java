package com.chaincat.pay.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chaincat.pay.model.enums.OrderStateEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单
 *
 * @author chenhaizhuang
 */
@Data
public class PayOrder {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID，表唯一键
     */
    private String orderId;

    /**
     * 订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭
     */
    private OrderStateEnum orderState;

    /**
     * 订单金额，单位元
     */
    private BigDecimal orderAmount;

    /**
     * 商品描述，简略描述，详细描述在业务方存储
     */
    private String description;

    /**
     * 支付第三方名称，定义业务方所使用的支付方式与支付系统实现类映射
     */
    private String payTpName;

    /**
     * 支付第三方应用ID
     */
    private String payTpAppId;

    /**
     * 支付第三方订单ID
     */
    private String payTpOrderId;

    /**
     * 支付第三方用户OpenID
     */
    private String payTpOpenId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

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
