package com.chaincat.base.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包余额交易
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceTransaction {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 钱包余额ID
     */
    private String walletBalanceId;

    /**
     * 关联ID
     */
    private Long relateId;

    /**
     * 外部交易ID
     */
    private String outTradeId;

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
     * 交易通知地址
     */
    private String notifyUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
