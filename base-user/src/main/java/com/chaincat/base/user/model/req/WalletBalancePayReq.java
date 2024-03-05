package com.chaincat.base.user.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 钱包余额支付请求
 *
 * @author chenhaizhuang
 */
@Data
public class WalletBalancePayReq {

    /**
     * 钱包密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 交易签名
     */
    @NotBlank(message = "交易签名")
    private String signature;
}
