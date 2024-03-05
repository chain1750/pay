package com.chaincat.base.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaincat.base.user.dao.entity.WalletBalance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包余额Mapper
 *
 * @author chenhaizhuang
 */
@Mapper
public interface WalletBalanceMapper extends BaseMapper<WalletBalance> {
}
