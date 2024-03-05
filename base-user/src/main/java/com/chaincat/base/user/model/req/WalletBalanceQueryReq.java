package com.chaincat.base.user.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 钱包余额交易查询请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalanceQueryReq {

    /**
     * 外部交易ID
     */
    @NotBlank(message = "外部交易ID")
    private String outTradeId;
}
