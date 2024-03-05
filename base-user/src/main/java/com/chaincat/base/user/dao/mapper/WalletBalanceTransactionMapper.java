package com.chaincat.base.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaincat.base.user.dao.entity.WalletBalanceTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包余额交易Mapper
 *
 * @author chenhaizhuang
 */
@Mapper
public interface WalletBalanceTransactionMapper extends BaseMapper<WalletBalanceTransaction> {
}
