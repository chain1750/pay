package com.chaincat.base.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaincat.base.user.dao.entity.Wallet;
import com.chaincat.base.user.dao.entity.WalletBalance;
import com.chaincat.base.user.dao.entity.WalletBalanceTransaction;
import com.chaincat.base.user.dao.mapper.WalletBalanceMapper;
import com.chaincat.base.user.dao.mapper.WalletBalanceTransactionMapper;
import com.chaincat.base.user.dao.mapper.WalletMapper;
import com.chaincat.base.user.exception.BizException;
import com.chaincat.base.user.model.enums.WalletTradeStateEnum;
import com.chaincat.base.user.model.req.WalletBalanceCloseReq;
import com.chaincat.base.user.model.req.WalletBalancePrepayReq;
import com.chaincat.base.user.model.req.WalletBalanceQueryReq;
import com.chaincat.base.user.model.req.WalletBalanceRefundReq;
import com.chaincat.base.user.model.resp.WalletBalancePrepayResp;
import com.chaincat.base.user.model.resp.WalletBalanceTransactionResp;
import com.chaincat.base.user.service.WalletBalanceTransactionService;
import com.chaincat.base.user.utils.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 钱包余额交易Service
 *
 * @author chenhaizhuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletBalanceTransactionServiceImpl implements WalletBalanceTransactionService {

    private final WalletMapper walletMapper;

    private final WalletBalanceMapper walletBalanceMapper;

    private final WalletBalanceTransactionMapper walletBalanceTransactionMapper;

    @Override
    public WalletBalancePrepayResp prepay(WalletBalancePrepayReq req) {
        // 查询数据
        Wallet wallet = walletMapper.selectOne(Wrappers.lambdaQuery(Wallet.class)
                .eq(Wallet::getUserId, req.getUserId()));
        Assert.notNull(wallet, "钱包未开通");
        WalletBalance walletBalance = walletBalanceMapper.selectOne(Wrappers.lambdaQuery(WalletBalance.class)
                .eq(WalletBalance::getWalletId, wallet.getId()));
        Assert.notNull(walletBalance, "钱包未开通");
        // 创建交易
        LocalDateTime now = LocalDateTime.now();
        WalletBalanceTransaction transaction = new WalletBalanceTransaction();
        transaction.setWalletBalanceId(walletBalance.getId());
        transaction.setOutTradeId(req.getOutTradeId());
        transaction.setTradeId(IdUtils.generate(now));
        transaction.setTradeAmount(req.getTradeAmount());
        transaction.setDescription(req.getDescription());
        transaction.setTradeState(WalletTradeStateEnum.PROCESSING.name());
        transaction.setNotifyUrl(req.getNotifyUrl());
        transaction.setCreateTime(now);
        transaction.setUpdateTime(now);
        walletBalanceTransactionMapper.insert(transaction);
        // 加密交易
        WalletBalancePrepayResp resp = new WalletBalancePrepayResp();
        resp.setSignature("");
        return resp;
    }

    @Override
    public void close(WalletBalanceCloseReq req) {
        // 查询数据
        WalletBalanceTransaction transaction = walletBalanceTransactionMapper.selectOne(
                Wrappers.lambdaQuery(WalletBalanceTransaction.class)
                        .eq(WalletBalanceTransaction::getOutTradeId, req.getOutTradeId()));
        Assert.notNull(transaction, "交易不存在");
        // 交易成功或失败不能关闭交易
        if (!WalletTradeStateEnum.PROCESSING.name().equals(transaction.getTradeState())) {
            throw new BizException("交易已结束，不允许关闭交易");
        }
        // 更新交易为失败
        transaction.setTradeState(WalletTradeStateEnum.FAIL.name());
        transaction.setUpdateTime(LocalDateTime.now());
        walletBalanceTransactionMapper.updateById(transaction);
    }

    @Override
    public WalletBalanceTransactionResp query(WalletBalanceQueryReq req) {
        // 查询数据
        WalletBalanceTransaction transaction = walletBalanceTransactionMapper.selectOne(
                Wrappers.lambdaQuery(WalletBalanceTransaction.class)
                        .eq(WalletBalanceTransaction::getOutTradeId, req.getOutTradeId()));
        Assert.notNull(transaction, "交易不存在");
        // 返回结果
        return BeanUtil.copyProperties(transaction, WalletBalanceTransactionResp.class);
    }

    @Override
    public void refund(WalletBalanceRefundReq req) {
        // 查询数据
        WalletBalanceTransaction transaction = walletBalanceTransactionMapper.selectOne(
                Wrappers.lambdaQuery(WalletBalanceTransaction.class)
                        .eq(WalletBalanceTransaction::getOutTradeId, req.getOutTradeId()));
        Assert.notNull(transaction, "交易不存在");
        WalletBalance walletBalance = walletBalanceMapper.selectById(transaction.getWalletBalanceId());
        Assert.notNull(walletBalance, "钱包未开通");
        // 查询交易下所有的退款
        List<WalletBalanceTransaction> walletBalanceTransactions = walletBalanceTransactionMapper.selectList(
                Wrappers.lambdaQuery(WalletBalanceTransaction.class)
                        .eq(WalletBalanceTransaction::getRelateId, transaction.getId()));
        // 总退款金额
        BigDecimal allRefundAmount = walletBalanceTransactions.stream()
                .map(WalletBalanceTransaction::getTradeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(req.getRefundAmount());
        if (allRefundAmount.compareTo(transaction.getTradeAmount()) > 0) {
            throw new BizException("已退款金额大于交易金额");
        }
        // 创建交易
        LocalDateTime now = LocalDateTime.now();
        WalletBalanceTransaction refundTransaction = new WalletBalanceTransaction();
        refundTransaction.setWalletBalanceId(walletBalance.getId());
        refundTransaction.setRelateId(transaction.getId());
        refundTransaction.setOutTradeId(req.getOutTradeId());
        refundTransaction.setTradeId(IdUtils.generate(now));
        refundTransaction.setTradeAmount(req.getRefundAmount());
        refundTransaction.setDescription(req.getRefundReason());
        refundTransaction.setTradeState(WalletTradeStateEnum.PROCESSING.name());
        refundTransaction.setNotifyUrl(req.getNotifyUrl());
        refundTransaction.setCreateTime(now);
        refundTransaction.setUpdateTime(now);
        walletBalanceTransactionMapper.insert(transaction);
    }
}
