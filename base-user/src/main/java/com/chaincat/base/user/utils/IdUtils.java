package com.chaincat.base.user.utils;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ID工具
 *
 * @author chenhaizhuang
 */
public class IdUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmssS");

    public static String generate(LocalDateTime now) {
        return now.format(FORMATTER) + IdWorker.getIdStr();
    }
}
