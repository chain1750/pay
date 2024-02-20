package com.chaincat.pay.model.base;

import lombok.Data;

/**
 * 接口结果
 *
 * @author chenhaizhuang
 */
@Data
public class ApiResult<T> {

    /**
     * 结果码
     */
    private Integer code;

    /**
     * 结果信息
     */
    private String msg;

    /**
     * 接口数据
     */
    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(0);
        apiResult.setMsg("OK");
        apiResult.setData(data);
        return apiResult;
    }

    public static ApiResult<Void> success() {
        ApiResult<Void> apiResult = new ApiResult<>();
        apiResult.setCode(0);
        apiResult.setMsg("OK");
        return apiResult;
    }

    public static ApiResult<Void> fail(String msg) {
        ApiResult<Void> apiResult = new ApiResult<>();
        apiResult.setCode(-1);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static ApiResult<Void> error() {
        ApiResult<Void> apiResult = new ApiResult<>();
        apiResult.setCode(-10);
        apiResult.setMsg("ERROR");
        return apiResult;
    }
}
