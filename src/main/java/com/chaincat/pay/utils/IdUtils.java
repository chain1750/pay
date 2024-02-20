package com.chaincat.pay.utils;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ID工具
 *
 * @author chenhaizhuang
 */
public class IdUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public static final String PREFIX_ORDER = "1";

    public static final String PREFIX_REFUND = "2";

    public static String generate(String prefix, LocalDateTime now) {
        return prefix + now.format(FORMATTER) + IdWorker.getIdStr();
    }
}
