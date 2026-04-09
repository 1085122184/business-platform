package com.cjx.uibot.service.schedule;

import com.cjx.uibot.service.entity.uibot.Task;
import com.cjx.uibot.service.manager.TaskExecutionManager;
import com.cjx.uibot.service.repository.uibot.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 定时任务调度器
 * 负责任务超时检测和历史数据清理
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final TaskRepository taskRepository;
    private final TaskExecutionManager executionManager;

    /**
     * 检查超时任务
     * 每分钟执行一次，检查是否有任务执行超时
     */
    @Scheduled(fixedDelayString = "${app.task-queue.timeout.check-interval:60000}")
    @ConditionalOnProperty(name = "task-queue.timeout.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional(rollbackFor = Exception.class)
    public void checkTimeoutTasks() {
        log.debug("开始检查超时任务...");

        try {
            // 计算超时阈值时间
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusSeconds(300); // 默认5分钟超时

            // 查询超时的RUNNING任务
            List<Task> timeoutTasks = taskRepository.findTimeoutTasks(Task.TaskStatus.RUNNING, timeoutThreshold);

            if (timeoutTasks.isEmpty()) {
                log.debug("没有超时任务");
                return;
            }

            log.warn("发现 {} 个超时任务", timeoutTasks.size());

            // 处理超时任务
            for (Task task : timeoutTasks) {
                handleTimeoutTask(task);
            }

            log.info("超时任务处理完成，共处理 {} 个任务", timeoutTasks.size());

        } catch (Exception e) {
            log.error("检查超时任务失败", e);
        }
    }

    /**
     * 处理单个超时任务
     */
    private void handleTimeoutTask(Task task) {
        log.warn("任务执行超时: taskId={}, taskName={}, startedAt={}",
                task.getId(), task.getTaskName(), task.getStartedAt());

        // 更新任务状态为超时
        task.setStatus(Task.TaskStatus.TIMEOUT);
        task.setCompletedAt(LocalDateTime.now());
        task.setErrorMessage("任务执行超时");
        taskRepository.save(task);

        // 如果该任务正在执行管理器中，强制清除
        String currentTaskId = executionManager.getCurrentTaskId();
        if (task.getId().equals(currentTaskId)) {
            executionManager.forceClear("任务超时");
            log.warn("已强制清除超时任务的执行锁: taskId={}", task.getId());
        }
    }

    /**
     * 清理历史任务数据
     * 每天凌晨2点执行，删除30天前已完成或失败的任务
     */
    @Scheduled(cron = "${app.task-queue.cleanup.schedule:0 0 2 * * ?}")
    @ConditionalOnProperty(name = "task-queue.cleanup.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional(rollbackFor = Exception.class)
    public void cleanupOldTasks() {
        log.info("开始清理历史任务数据...");

        try {
            // 计算清理截止日期（默认30天前）
            int retentionDays = 30;
            LocalDateTime beforeDate = LocalDateTime.now().minusDays(retentionDays);

            // 要删除的状态
            List<Task.TaskStatus> cleanupStatuses = Arrays.asList(
                    Task.TaskStatus.COMPLETED,
                    Task.TaskStatus.FAILED,
                    Task.TaskStatus.TIMEOUT,
                    Task.TaskStatus.CANCELLED
            );

            // 执行删除
            int deletedCount = taskRepository.deleteOldCompletedTasks(beforeDate, cleanupStatuses);

            if (deletedCount > 0) {
                log.info("清理历史任务完成，删除 {} 条记录（{}天前）", deletedCount, retentionDays);
            } else {
                log.info("没有需要清理的历史任务");
            }

        } catch (Exception e) {
            log.error("清理历史任务失败", e);
        }
    }

    /**
     * 系统健康检查
     * 每10分钟检查一次系统状态
     */
    @Scheduled(fixedDelay = 600000) // 10分钟
    public void healthCheck() {
        log.debug("执行系统健康检查...");

        try {
            // 检查是否有异常状态
            long runningCount = taskRepository.countByStatus(Task.TaskStatus.RUNNING);
            String currentTaskId = executionManager.getCurrentTaskId();

            // 如果数据库显示有RUNNING任务，但执行管理器中没有，说明状态不一致
            if (runningCount > 0 && currentTaskId == null) {
                log.error("检测到状态不一致：数据库中有RUNNING任务，但执行管理器中无任务");
                // 可以在这里添加自动修复逻辑
            }

            // 如果执行管理器中有任务，但数据库中没有对应的RUNNING任务
            if (currentTaskId != null && runningCount == 0) {
                log.error("检测到状态不一致：执行管理器中有任务，但数据库中无RUNNING任务");
                executionManager.forceClear("状态不一致");
            }

            log.debug("系统健康检查完成");

        } catch (Exception e) {
            log.error("健康检查失败", e);
        }
    }
}
