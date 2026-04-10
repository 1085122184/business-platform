package com.cjx.uibot.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.common.strategy.executor.StrategyExecutor;
import com.cjx.common.strategy.result.StrategyResult;
import com.cjx.uibot.entity.uibot.Task;
import com.cjx.uibot.service.impl.TaskService;
import dongyue.common.strategy.dto.http.ApiResponse;
import dongyue.common.strategy.dto.http.StrategyExecuteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 策略执行控制器
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/strategy")
@Schema(description = "策略执行接口")
@RequiredArgsConstructor
public class StrategyController {
    private final StrategyExecutor strategyExecutor;
    private final TaskService taskService;

    @PostMapping("test")
    public ApiResponse<Object> test(@Validated @RequestBody StrategyExecuteRequest request){
        // 构建执行上下文
        StrategyContext context = buildContext(request);

        // 执行策略
        StrategyResult<?> result = strategyExecutor.execute(request.getStrategyKey(), context);

        // 构建响应
        return buildResponse(result);
    }

    @PostMapping("get/{name}")
    public ApiResponse<Object> getTest(@PathVariable("name") String name){
        Map<String, Object> params = new HashMap<>();
        params.put("name","123");
        // 构建执行上下文
        StrategyContext context = StrategyContext.builder().params(params).traceId(MDC.get("traceId")).build();
        // 执行策略
        StrategyResult<?> result = strategyExecutor.execute(name, context);

        // 构建响应
        return buildResponse(result);
    }


    //todo 获取数据时，根据策略执行不同的方法待修改
    @GetMapping("/getData")
    public Map<String,String> getData(){
        Map<String,String> resultMap = new HashMap<>();
        String taskId = taskService.getCurrentExecutingTaskId();
        resultMap.put("id",taskId);
        Task task = taskService.getTask(taskId);
        JSONObject jsonObject = JSONUtil.parseObj(task.getTaskData());
        System.out.println(jsonObject.get("tableName"));
        return resultMap;
    }


    /**
     * 构建执行上下文
     */
    private StrategyContext buildContext(StrategyExecuteRequest request) {
        return StrategyContext.builder()
                .params(request.getParams())
                .extras(request.getExtras())
                .traceId(MDC.get("traceId"))
                .build();
    }

    /**
     * 构建响应
     */
    private ApiResponse<Object> buildResponse(StrategyResult<?> result) {
        ApiResponse<Object> response;
        if (Boolean.TRUE.equals(result.getSuccess())) {
            response = ApiResponse.success(result.getData());
        } else {
            response = ApiResponse.failure(
                    result.getErrorCode(),
                    result.getErrorMessage());
        }
        response.setTraceId(result.getTraceId());
        return response;
    }

    /**
     * 获取用户ID（从Token、Session等）
     */
    private String getUserId(HttpServletRequest request) {
        // 实际项目中从JWT Token或Session中获取
        String userId = request.getHeader("X-User-Id");
        return userId != null ? userId : "anonymous";
    }

    /**
     * 获取用户名
     */
    private String getUsername(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        return username != null ? username : "anonymous";
    }

    /**
     * 获取租户ID
     */
    private String getTenantId(HttpServletRequest request) {
        return request.getHeader("X-Tenant-Id");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
