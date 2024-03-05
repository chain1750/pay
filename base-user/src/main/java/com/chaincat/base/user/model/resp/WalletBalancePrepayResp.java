package com.chaincat.base.user.model.resp;

import lombok.Data;

/**
 * 钱包余额预支付结果
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalancePrepayResp {

    /**
     * 签名
     */
    private String signature;
}
