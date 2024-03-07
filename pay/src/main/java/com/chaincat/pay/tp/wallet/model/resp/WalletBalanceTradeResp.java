package com.chaincat.pay.tp.wallet.model.resp;

import cn.hutool.core.date.DatePattern;
import com.chaincat.pay.tp.wallet.model.enums.TradeStateEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 钱包余额交易结果
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceTradeResp {

    /**
     * 外部交易ID
     */
    private String outTradeId;

    /**
     * 交易ID
     */
    private String tradeId;

    /**
     * 交易状态：PROCESSING-处理中，SUCCESS-已支付，CLOSED-已关闭
     */
    private TradeStateEnum tradeState;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private LocalDateTime tradeTime;
}
