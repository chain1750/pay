package com.chaincat.base.user.model.resp;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 钱包余额交易结果
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceTransactionResp {

    /**
     * 外部交易ID
     */
    private String outTradeId;

    /**
     * 交易ID，表唯一键
     */
    private String tradeId;

    /**
     * 交易状态：PROCESSING-处理中，SUCCESS-交易成功，FAIL-交易失败
     */
    private String tradeState;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private LocalDateTime tradeTime;
}
