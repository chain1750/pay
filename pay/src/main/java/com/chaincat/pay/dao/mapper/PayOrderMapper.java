package com.chaincat.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaincat.pay.dao.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单Mapper
 *
 * @author chenhaizhuang
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
