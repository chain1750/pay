package com.chaincat.pay.product.wechat;

import cn.hutool.core.date.DatePattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 微信工具
 *
 * @author chenhaizhuang
 */
public class WeChatUtils {

    /**
     * 获取金额，单位分
     *
     * @param amount 金额
     * @return Integer
     */
    public static Integer getAmountInt(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * 获取金额，单位分
     *
     * @param amount 金额
     * @return Long
     */
    public static Long getAmountLong(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 获取时间字符串
     *
     * @param time LocalDateTime
     * @return String
     */
    public static String getTimeStr(LocalDateTime time) {
        return time.atOffset(ZoneOffset.of("+08:00"))
                .format(DateTimeFormatter.ofPattern(DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
    }

    /**
     * 获取时间
     *
     * @param time 时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime getTime(String time) {
        OffsetDateTime offsetDateTime = OffsetDateTime
                .parse(time, DateTimeFormatter.ofPattern(DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
        return offsetDateTime.toLocalDateTime();
    }
}
