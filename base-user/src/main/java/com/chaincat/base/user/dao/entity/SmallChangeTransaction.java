package com.chaincat.base.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 零钱交易
 *
 * @author chenhaizhuang
 */
@Data
public class SmallChangeTransaction {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 零钱唯一标识
     */
    private String scId;

    /**
     * 交易ID，表唯一键
     */
    private String tradeId;

    /**
     * 交易金额，负数为支出，正数为收入
     */
    private BigDecimal tradeAmount;

    /**
     * 当前零钱余额
     */
    private BigDecimal balance;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 交易状态：PROCESSING-处理中，SUCCESS-交易成功，FAIL-交易失败
     */
    private String tradeState;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
