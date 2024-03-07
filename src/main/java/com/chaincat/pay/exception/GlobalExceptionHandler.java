package com.chaincat.pay.exception;

import com.alibaba.fastjson.JSON;
import com.chaincat.pay.model.base.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author chenhaizhuang
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     *
     * @param e 异常
     * @return Result
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBindException(BindException e) {
        List<ObjectError> allErrors = e.getAllErrors();
        List<String> errorMsgList = allErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        String errorMsg = JSON.toJSONString(errorMsgList);
        log.warn("参数校验异常：{}", errorMsg);
        return ApiResult.fail(errorMsg);
    }

    /**
     * 处理业务异常
     *
     * @param e 异常
     * @return Result
     */
    @ExceptionHandler({BizException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ApiResult<Void> handleBizException(RuntimeException e) {
        log.warn("业务异常：{}", e.getMessage(), e);
        return ApiResult.fail(e.getMessage());
    }

    /**
     * 处理全局异常
     *
     * @param e 异常
     * @return Result
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleGlobalException(Exception e) {
        log.error("全局异常：{}", e.getMessage(), e);
        return ApiResult.error();
    }
}
