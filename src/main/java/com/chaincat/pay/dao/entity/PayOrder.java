package com.chaincat.pay.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
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
     * 用户IP，当前下单用户所在IP
     */
    private String userIp;

    /**
     * 用户ID，当前下单用户在系统中的唯一ID
     */
    private String userId;

    /**
     * 订单ID，表唯一键，固定32位
     */
    private String orderId;

    /**
     * 订单状态：NOT_PAY-未支付，SUCCESS-已支付，CLOSED-已关闭
     */
    private String orderState;

    /**
     * 订单金额，单位元
     */
    private BigDecimal orderAmount;

    /**
     * 商品描述，简略描述，详细描述在业务方存储
     */
    private String description;

    /**
     * 产品名称，用于避免不同渠道上应用ID重复，命名格式：渠道_产品名称（大写字母）
     */
    private String productName;

    /**
     * 产品应用ID
     */
    private String productAppId;

    /**
     * 产品订单ID
     */
    private String productOrderId;

    /**
     * 产品应用用户OpenId
     */
    private String productOpenId;

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

    /**
     * 订单附加信息，针对不同支付渠道所需参数的差异，采用json字符串格式传参
     */
    private String orderAttach;
}
