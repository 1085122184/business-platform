package com.cjx.uibot.controller;

import cn.hutool.json.JSONUtil;
import com.cjx.common.core.utils.BeanConverterUtil;
import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.common.strategy.executor.StrategyExecutor;
import com.cjx.common.strategy.result.StrategyResult;
import com.cjx.uibot.api.dto.task.TaskCompleteRequest;
import com.cjx.uibot.api.dto.task.TaskSubmitRequest;
import com.cjx.uibot.api.enums.QueueType;
import com.cjx.uibot.api.vo.task.SystemStatusResponse;
import com.cjx.uibot.api.vo.task.TaskCallbackResponse;
import com.cjx.uibot.api.vo.task.TaskDetailResponse;
import com.cjx.uibot.api.vo.task.TaskSubmitResponse;
import com.cjx.uibot.entity.uibot.Task;
import com.cjx.uibot.service.impl.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 任务管理控制器
 * 提供任务提交、查询、回调等接口
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
@Tag(name = "任务管理", description = "任务的创建、查询和状态管理接口")
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskService taskService;
    private final StrategyExecutor strategyExecutor;

    /**
     * 提交新任务
     *
     * @param request 任务提交请求
     * @return 任务提交响应
     */
    @PostMapping("/submit")
    @Operation(summary = "提交新任务", description = "提交一个新任务到系统，如果当前无任务执行则立即执行，否则加入队列")
    public ResponseEntity<TaskSubmitResponse> submitTask(
            @Valid @RequestBody TaskSubmitRequest request) {

        log.info("接收到任务提交请求: {}", request);
        try {
            // 解析队列类型
            QueueType queueType;
            try {
                queueType = QueueType.valueOf(
                        request.getQueueType() != null ? request.getQueueType() : "NORMAL"
                );
            } catch (IllegalArgumentException e) {
                log.warn("无效的队列类型: {}, 使用默认的NORMAL", request.getQueueType());
                queueType = QueueType.NORMAL;
            }
            // 创建任务
            Task task = taskService.submitTask(request.getTaskName(),request.getTaskData(),request.getPriority(),queueType);
            boolean isQueued = task.getStatus() == Task.TaskStatus.QUEUED;
            int queuePosition = isQueued ? taskService.getQueueSize() : 0;

            TaskSubmitResponse response = TaskSubmitResponse.builder()
                    .taskId(task.getId())
                    .status(isQueued ? "queued" : "executing")
                    .message(isQueued
                            ? String.format("当前有任务正在执行，已加入队列等待，当前队列位置: %d", queuePosition)
                            : "任务已开始执行")
                    .queued(isQueued)
                    .queuePosition(queuePosition)
                    .timestamp(System.currentTimeMillis())
                    .build();

            log.info("任务提交成功: taskId={}, queued={}", task.getId(), isQueued);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("提交任务失败", e);
            throw e;
        }
    }

    /**
     * 任务完成回调接口
     * 外部应用执行完任务后调用此接口通知系统
     *
     * @param request 任务完成请求
     * @return 回调响应
     */
    @PostMapping("/callback/complete")
    @Operation(summary = "任务完成回调", description = "外部应用执行完任务后调用此接口通知系统")
    public ResponseEntity<TaskCallbackResponse> completeTask(
            @Valid @RequestBody TaskCompleteRequest request) {

        log.info("接收到任务完成回调: taskId={}, success={}",
                request.getTaskId(), request.getSuccess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        taskService.completeTask(
                request.getTaskId(),
                request.getSuccess(),
                request.getErrorMessage()
        );
        try {
            TaskCallbackResponse response = TaskCallbackResponse.builder()
                    .taskId(request.getTaskId())
                    .status("success")
                    .message("任务状态已更新，正在处理下一个任务")
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理任务回调失败: taskId={}", request.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 查询任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "查询任务详情", description = "根据任务ID查询任务的详细信息")
    public ResponseEntity<TaskDetailResponse> getTask(
            @Parameter(description = "任务ID") @PathVariable String taskId) {

        log.debug("查询任务详情: taskId={}", taskId);

        Task task = taskService.getTask(taskId);
        TaskDetailResponse response = BeanConverterUtil.convert(task,TaskDetailResponse.class);
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询任务列表
     *
     * @param status 任务状态（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 任务列表
     */
    @GetMapping
    @Operation(summary = "查询任务列表", description = "分页查询任务列表，可按状态筛选")
    public ResponseEntity<Page<TaskDetailResponse>> getTasks(
            @Parameter(description = "任务状态") @RequestParam(required = false) Task.TaskStatus status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        log.debug("查询任务列表: status={}, page={}, size={}", status, page, size);

        Page<Task> tasks = taskService.getTasks(status, page, size);
        Page<TaskDetailResponse> response = tasks.map(BeanConverterUtil.toConverter(TaskDetailResponse.class));

        return ResponseEntity.ok(response);
    }

    /**
     * 查询系统状态
     *
     * @return 系统状态信息
     */
    @GetMapping("/system/status")
    @Operation(summary = "查询系统状态", description = "查询当前队列大小和执行中的任务信息")
    public ResponseEntity<SystemStatusResponse> getSystemStatus() {

        log.debug("查询系统状态");

        SystemStatusResponse response = SystemStatusResponse.builder()
                .queueSize(taskService.getQueueSize())
                .priorityQueueSize(taskService.getPriorityQueueSize())
                .normalQueueSize(taskService.getNormalQueueSize())
                .currentTaskId(taskService.getCurrentExecutingTaskId())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test")
    public void test(@RequestBody TaskCompleteRequest request){
        System.out.println(request.getSuccess());

    }
    @PostMapping("/test/submit")
    public void testSubmit(@RequestBody TaskSubmitRequest request){
        System.out.println(request.getTaskData());

    }

    //todo 获取数据时，根据策略执行不同的方法待修改
    @GetMapping("/getData")
    public Map<String,Object> getData(){
        String taskId = taskService.getCurrentExecutingTaskId();
        Task task = taskService.getTask(taskId);
        Map<String, Object> resultMap = JSONUtil.parseObj(task.getTaskData());
        resultMap.put("key",resultMap.get("tableName")+"-get");
        StrategyResult<?> result = strategyExecutor.execute(String.valueOf(resultMap.get("key")), StrategyContext.builder().params(resultMap).build());
        resultMap.put("id",taskId);
        resultMap.put("data",result.getData());
        return resultMap;
    }

}
