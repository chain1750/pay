package com.chaincat.pay.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chaincat.pay.model.enums.RefundStateEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付退款
 *
 * @author chenhaizhuang
 */
@Data
public class PayRefund {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID，关联pay_order
     */
    private String orderId;

    /**
     * 退款ID，表唯一键
     */
    private String refundId;

    /**
     * 退款状态：PROCESSING-处理中，SUCCESS-成功，FAIL-失败
     */
    private RefundStateEnum refundState;

    /**
     * 退款金额，不能大于订单金额，单位元
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因，简略描述，详细描述在业务方存储
     */
    private String refundReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 支付第三方退款ID
     */
    private String payTpRefundId;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 支付订单
     */
    @TableField(exist = false)
    private PayOrder payOrder;
}
