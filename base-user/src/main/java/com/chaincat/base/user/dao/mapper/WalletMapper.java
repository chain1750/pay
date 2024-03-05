package com.chaincat.base.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaincat.base.user.dao.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包Mapper
 *
 * @author chenhaizhuang
 */
@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
}
