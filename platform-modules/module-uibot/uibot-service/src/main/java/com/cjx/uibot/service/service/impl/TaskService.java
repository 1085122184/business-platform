package com.cjx.uibot.service.service.impl;

import com.alibaba.fastjson2.JSON;
import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.common.strategy.executor.StrategyExecutor;
import com.cjx.uibot.api.dto.task.QueueTask;
import com.cjx.uibot.api.enums.QueueType;
import com.cjx.uibot.service.entity.uibot.Task;
import com.cjx.uibot.service.manager.QueueManager;
import com.cjx.uibot.service.manager.TaskExecutionManager;
import com.cjx.uibot.service.repository.uibot.TaskRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现类
 * 负责任务的创建、执行、状态管理和队列调度
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final QueueManager queueManager;
    private final TaskExecutionManager executionManager;
    private final StrategyExecutor strategyExecutor;
    /**
     * 系统启动时初始化队列
     * 从数据库恢复未完成的任务到队列中
     */
    @PostConstruct
    public void initQueue() {
        log.info("开始初始化任务队列...");

        // 重置执行管理器状态
        executionManager.reset();

        try {
            // 查询所有未完成的任务
            List<Task.TaskStatus> incompleteStatuses = Arrays.asList(
                    Task.TaskStatus.PENDING,
                    Task.TaskStatus.QUEUED,
                    Task.TaskStatus.RUNNING
            );

            List<Task> incompleteTasks = taskRepository
                    .findIncompleteTasksOrderByPriority(incompleteStatuses);

            if (incompleteTasks.isEmpty()) {
                log.info("没有需要恢复的任务");
                return;
            }

            log.info("发现 {} 个未完成任务，开始恢复...", incompleteTasks.size());

            // 将RUNNING状态的任务重置为PENDING
            incompleteTasks.stream()
                    .filter(task -> task.getStatus() == Task.TaskStatus.RUNNING)
                    .forEach(task -> {
                        task.setStatus(Task.TaskStatus.PENDING);
                        task.setStartedAt(null);
                        taskRepository.save(task);
                        log.warn("将异常中断的RUNNING任务重置为PENDING: taskId={}", task.getId());
                    });

            // 将所有未完成任务加入队列
            incompleteTasks.forEach(task -> {
                QueueTask queueTask = QueueTask.builder()
                        .taskId(task.getId())
                        .priority(task.getPriority())
                        .createdAt(task.getCreateTime())
                        .enqueuedAt(LocalDateTime.now())
                        .queueType(task.getQueueType())
                        .build();

                queueManager.offer(queueTask);

                // 更新状态为QUEUED（如果不是已经是QUEUED）
                if (task.getStatus() != Task.TaskStatus.QUEUED) {
                    task.setStatus(Task.TaskStatus.QUEUED);
                    taskRepository.save(task);
                }
            });

            log.info("任务队列初始化完成，优先级队列: {}, 普通队列: {}",
                    queueManager.getPriorityQueueSize(), queueManager.getNormalQueueSize());

            // 尝试执行第一个任务
            processNextTask();

        } catch (Exception e) {
            log.error("初始化任务队列失败", e);
            throw new RuntimeException("任务队列初始化失败", e);
        }
    }

    /**
     * 提交新任务
     *
     * @param taskName 任务名称
     * @param taskData 任务数据
     * @param priority 优先级
     * @param queueType 队列类型
     * @return 创建的任务
     */
    @Transactional(rollbackFor = Exception.class)
    public Task submitTask(String taskName, String taskData, Integer priority, QueueType queueType ) {
        log.info("提交任务: taskName={}, priority={}, queueType={}", taskName, priority, queueType);
        // 参数校验
        validateTaskParameters(taskName, priority);
        Task task = Task.builder()
                .taskName(taskName)
                .taskData(taskData)
                .priority(priority)
                .status(Task.TaskStatus.PENDING)
                .retryCount(0)
                .queueType(queueType)
                .build();
        // 保存到数据库
        task = taskRepository.save(task);
        log.debug("任务已保存到数据库: taskId={}", task.getId());
        // 检查是否有任务正在执行
        if (executionManager.isTaskExecuting()) {
            // 有任务执行，加入队列
            addTaskToQueue(task);
            log.info("当前有任务执行，任务已加入{}，总队列大小: {}",
                    task.getQueueType().getDescription(), queueManager.getTotalQueueSize());
        } else {
            // 没有任务执行，立即执行
            startTaskExecution(task);
            log.info("立即开始执行任务: taskId={}", task.getId());
        }

        return task;
    }

    /**
     * 任务完成回调
     *
     * @param taskId 任务ID
     * @param success 是否成功
     * @param errorMessage 错误信息（失败时）
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, boolean success, String errorMessage) {
        log.info("收到任务完成回调: taskId={}, success={}", taskId, success);

        // 查询任务
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        // 验证任务状态
        if (task.getStatus() != Task.TaskStatus.RUNNING) {
            log.warn("任务状态异常，不是RUNNING状态: taskId={}, status={}",
                    taskId, task.getStatus());
            throw new RuntimeException("任务状态异常，当前状态: " + task.getStatus());
        }

        // 更新任务状态
        task.setCompletedAt(LocalDateTime.now());
        if (success) {
            task.setStatus(Task.TaskStatus.COMPLETED);
            log.info("任务执行成功: taskId={}", taskId);
        } else {
            task.setStatus(Task.TaskStatus.FAILED);
            task.setErrorMessage(errorMessage);
            log.error("任务执行失败: taskId={}, error={}", taskId, errorMessage);
        }

        taskRepository.save(task);

        // 释放执行锁
        if (!executionManager.finishExecution(taskId)) {
            log.warn("释放执行锁失败，可能任务ID不匹配: taskId={}", taskId);
        }
        // 处理下一个任务
        processNextTask();
    }

    /**
     * 处理队列中的下一个任务
     */
    private void processNextTask() {
        // 检查队列是否为空
        if (queueManager.isEmpty()) {
            log.info("队列为空，无待执行任务");
            return;
        }

        // 检查是否有任务在执行
        if (executionManager.isTaskExecuting()) {
            log.debug("当前有任务执行中，等待完成");
            return;
        }

        // 从队列取出任务（优先从优先级队列取）
        QueueTask queueTask = queueManager.poll();
        if (queueTask == null) {
            log.debug("队列为空（并发竞争），无任务可执行");
            return;
        }

        log.info("从队列取出任务: taskId={}, queueType={}, 剩余队列大小 - 优先级: {}, 普通: {}",
                queueTask.getTaskId(), queueTask.getQueueType(),
                queueManager.getPriorityQueueSize(), queueManager.getNormalQueueSize());

        // 查询完整任务信息
        Task task = taskRepository.findById(queueTask.getTaskId())
                .orElse(null);

        if (task == null) {
            log.warn("任务不存在，跳过: taskId={}", queueTask.getTaskId());
            processNextTask(); // 继续处理下一个
            return;
        }

        // 检查任务状态
        if (task.isTerminalState()) {
            log.warn("任务已处于终态，跳过: taskId={}, status={}",
                    task.getId(), task.getStatus());
            processNextTask(); // 继续处理下一个
            return;
        }

        // 开始执行任务
        startTaskExecution(task);
    }

    /**
     * 开始执行任务
     *
     * @param task 任务
     */
    public void startTaskExecution(Task task) {
        String taskId = task.getId();

        // 尝试获取执行权限
        if (!executionManager.tryStartExecution(taskId)) {
            // 获取失败，加入队列
            addTaskToQueue(task);
            log.warn("获取执行权限失败，任务加入队列: taskId={}", taskId);
            return;
        }

        try {
            // 更新任务状态
            task.setStatus(Task.TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());
            taskRepository.save(task);
            Map<String, Object> params = JSON.parseObject(task.getTaskData());
            strategyExecutor.execute(params.get("type").toString(), StrategyContext.builder().params(params).build());
            log.info("任务已开始执行，等待外部应用回调: taskId={}", taskId);

        } catch (Exception e) {
            log.error("启动任务执行失败: taskId={}", taskId, e);
            executionManager.finishExecution(taskId);
            throw e;
        }
    }

    /**
     * 将任务加入队列
     *
     * @param task 任务
     */
    private void addTaskToQueue(Task task) {
        QueueTask queueTask = QueueTask.builder()
                .taskId(task.getId())
                .priority(task.getPriority())
                .createdAt(task.getCreateTime())
                .enqueuedAt(LocalDateTime.now())
                .queueType(task.getQueueType())
                .build();

        queueManager.offer(queueTask);

        // 更新任务状态为QUEUED
        task.setStatus(Task.TaskStatus.QUEUED);
        taskRepository.save(task);

        log.debug("任务已加入队列: taskId={}, queueType={}, priority={}, 队列大小 - 优先级: {}, 普通: {}",
                task.getId(), task.getQueueType(), task.getPriority(),
                queueManager.getPriorityQueueSize(), queueManager.getNormalQueueSize());
    }

    /**
     * 参数校验
     */
    private void validateTaskParameters(String taskName, Integer priority) {
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new RuntimeException("任务名称不能为空");
        }
        if (taskName.length() > 200) {
            throw new RuntimeException("任务名称长度不能超过200");
        }
        if (priority == null || priority < 1 || priority > 10) {
            throw new RuntimeException("优先级必须在1-10之间");
        }
    }

    /**
     * 查询任务详情
     */
    @Transactional(readOnly = true)
    public Task getTask(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
    }

    /**
     * 分页查询任务列表
     */
    @Transactional(readOnly = true)
    public Page<Task> getTasks(Task.TaskStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createTime"));

        if (status != null) {
            return taskRepository.findByStatus(status, pageable);
        } else {
            return taskRepository.findAll(pageable);
        }
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return queueManager.getTotalQueueSize();
    }

    /**
     * 获取优先级队列大小
     */
    public int getPriorityQueueSize() {
        return queueManager.getPriorityQueueSize();
    }

    /**
     * 获取普通队列大小
     */
    public int getNormalQueueSize() {
        return queueManager.getNormalQueueSize();
    }

    /**
     * 获取当前执行任务ID
     */
    public String getCurrentExecutingTaskId() {
        return executionManager.getCurrentTaskId();
    }
}
