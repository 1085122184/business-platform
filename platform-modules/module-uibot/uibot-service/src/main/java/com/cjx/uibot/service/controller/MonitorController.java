package com.cjx.uibot.service.controller;

import com.cjx.common.core.utils.BeanConverterUtil;
import com.cjx.uibot.api.vo.task.MonitorStatsResponse;
import com.cjx.uibot.api.vo.task.TaskDetailResponse;
import com.cjx.uibot.service.entity.uibot.Task;
import com.cjx.uibot.service.repository.uibot.TaskRepository;
import com.cjx.uibot.service.service.impl.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控数据控制器
 * 提供系统监控所需的统计和状态数据
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Tag(name = "系统监控", description = "系统状态监控和统计数据接口")
@CrossOrigin(origins = "*")
public class MonitorController {
    private final TaskService taskService;
    private final TaskRepository taskRepository;

    /**
     * 获取系统统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取系统统计", description = "获取各状态任务数量统计")
    public ResponseEntity<MonitorStatsResponse> getStats() {

        log.debug("获取系统统计数据");

        MonitorStatsResponse response = MonitorStatsResponse.builder()
                .queueSize(taskService.getQueueSize())
                .priorityQueueSize(taskService.getPriorityQueueSize())
                .normalQueueSize(taskService.getNormalQueueSize())
                .pendingCount(taskRepository.countByStatus(Task.TaskStatus.PENDING))
                .queuedCount(taskRepository.countByStatus(Task.TaskStatus.QUEUED))
                .runningCount(taskRepository.countByStatus(Task.TaskStatus.RUNNING))
                .completedCount(taskRepository.countByStatus(Task.TaskStatus.COMPLETED))
                .failedCount(taskRepository.countByStatus(Task.TaskStatus.FAILED))
                .timeoutCount(taskRepository.countByStatus(Task.TaskStatus.TIMEOUT))
                .currentTaskId(taskService.getCurrentExecutingTaskId())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前执行的任务详情
     *
     * @return 当前任务详情，如果没有则返回null
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前执行任务", description = "获取当前正在执行的任务详情")
    public ResponseEntity<TaskDetailResponse> getCurrentTask() {

        log.debug("查询当前执行任务");

        String currentTaskId = taskService.getCurrentExecutingTaskId();

        if (currentTaskId == null) {
            return ResponseEntity.ok(null);
        }

        try {
            Task task = taskService.getTask(currentTaskId);
//            TaskDetailResponse response = TaskDetailResponse.fromEntity(task);
            TaskDetailResponse response = BeanConverterUtil.convert(task,TaskDetailResponse.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询当前任务失败: taskId={}", currentTaskId, e);
            return ResponseEntity.ok(null);
        }
    }

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

}
