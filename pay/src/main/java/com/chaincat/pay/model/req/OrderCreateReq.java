package com.chaincat.pay.model.req;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建订单请求
 *
 * @author chenhaizhuang
 */
@Data
public class OrderCreateReq {

    /**
     * 用户IP，当前下单用户所在IP
     */
    @NotBlank(message = "用户IP不能为空")
    private String userIp;

    /**
     * 用户ID，当前下单用户在系统中的唯一ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 订单金额，单位元
     */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额不能小于0.01")
    private BigDecimal orderAmount;

    /**
     * 商品描述，简略描述，详细描述在业务方存储
     */
    @NotBlank(message = "商品描述不能为空")
    private String description;

    /**
     * 产品名称，用于避免不同渠道上应用ID重复，命名格式：渠道_产品名称（大写字母）
     */
    @NotBlank(message = "产品名称不能为空")
    private String productName;

    /**
     * 产品应用ID
     */
    @NotBlank(message = "产品应用ID不能为空")
    private String productAppId;

    /**
     * 产品应用用户OpenId
     */
    @NotBlank(message = "产品应用用户OpenId不能为空")
    private String productOpenId;

    /**
     * 过期时间
     */
    @NotNull(message = "过期时间不能为空")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private LocalDateTime expireTime;

    /**
     * 业务名称，用于避免不同业务的业务ID重复，命名格式：模块_业务（大写字母）
     */
    @NotBlank(message = "业务名称不能为空")
    private String bizName;

    /**
     * 业务ID
     */
    @NotBlank(message = "业务ID不能为空")
    private String bizId;

    /**
     * 业务消息队列主题，支付回调时使用消息队列通知业务方，需要业务方做好消费动作
     */
    @NotBlank(message = "业务消息队列主题不能为空")
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
