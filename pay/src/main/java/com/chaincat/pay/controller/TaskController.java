package com.chaincat.pay.controller;

import com.chaincat.pay.model.base.ApiResult;
import com.chaincat.pay.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务接口，由定时任务服务触发
 *
 * @author chenhaizhuang
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 处理支付订单
     *
     * @return Result
     */
    @PostMapping("/handleOrder")
    public ApiResult<Void> handleOrder() {
        taskService.handleOrder();
        return ApiResult.success();
    }

    /**
     * 处理退款
     *
     * @return Result
     */
    @PostMapping("/handleRefund")
    public ApiResult<Void> handleRefund() {
        taskService.handleRefund();
        return ApiResult.success();
    }
}
