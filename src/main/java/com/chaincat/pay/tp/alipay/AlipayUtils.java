package com.chaincat.pay.tp.alipay;

import cn.hutool.core.date.DatePattern;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝工具
 *
 * @author chenhaizhuang
 */
public class AlipayUtils {

    /**
     * 获取时间字符串
     *
     * @param time LocalDateTime
     * @return String
     */
    public static String getTimeStr(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN));
    }

    /**
     * 获取时间
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime getTime(Date date) {
        Instant instant = date.toInstant();
        return instant.atOffset(ZoneOffset.of("+08:00")).toLocalDateTime();
    }

    /**
     * 获取时间
     *
     * @param timeStr 时间字符串
     * @return Date
     */
    public static Date getTime(String timeStr) {
        LocalDateTime parse = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN));
        Instant instant = parse.toInstant(ZoneOffset.of("+08:00"));
        return Date.from(instant);
    }

    /**
     * 获取时间
     *
     * @param timeStr 时间字符串
     * @return Date
     */
    public static Date getTimeWithMilli(String timeStr) {
        LocalDateTime parse = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        Instant instant = parse.toInstant(ZoneOffset.of("+08:00"));
        return Date.from(instant);
    }

    /**
     * 获取通知请求参数
     *
     * @param parameterMap 请求参数
     * @return Map
     */
    public static Map<String, String> getRequestParam(Map<String, String[]> parameterMap) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String name = entry.getKey();
            if ("sign_type".equals(name)) {
                continue;
            }
            String[] values = entry.getValue();
            params.put(name, String.join(",", values));
        }
        return params;
    }
}
