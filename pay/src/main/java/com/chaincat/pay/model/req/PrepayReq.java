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
 * 预支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class PrepayReq {

    /**
     * 用户IP
     */
    @NotBlank(message = "用户IP不能为空")
    private String userIp;

    /**
     * 用户ID
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
     * 支付第三方名称，定义业务方所使用的支付方式与支付系统实现类映射
     */
    @NotBlank(message = "支付第三方名称不能为空")
    private String payTpName;

    /**
     * 支付第三方应用ID
     */
    @NotBlank(message = "支付第三方应用ID不能为空")
    private String payTpAppId;

    /**
     * 支付第三方用户OpenID
     */
    @NotBlank(message = "支付第三方用户OpenID不能为空")
    private String payTpOpenId;

    /**
     * 过期时间
     */
    @NotNull(message = "过期时间不能为空")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private LocalDateTime expireTime;

    /**
     * 业务名称，用于避免不同业务的业务ID重复
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
}
