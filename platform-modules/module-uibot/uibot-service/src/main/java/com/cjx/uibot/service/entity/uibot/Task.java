package com.cjx.uibot.service.entity.uibot;

import com.cjx.common.jpa.entity.BaseEntity;
import com.cjx.uibot.api.enums.QueueType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 任务实体类
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Entity
@Table(name = "t_task", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_priority_created", columnList = "priority,create_by"),
        @Index(name = "idx_created_at", columnList = "create_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200")
    @Column(name = "task_name", length = 200, nullable = false)
    private String taskName;

    /**
     * 任务数据（JSON格式）
     */
    @Column(name = "task_data", columnDefinition = "TEXT")
    private String taskData;

    /**
     * 任务优先级（1-10，数字越小优先级越高）
     */
    @Min(value = 1, message = "优先级最小值为1")
    @Max(value = 10, message = "优先级最大值为10")
    @Column(name = "priority", nullable = false)
    private Integer priority;

    /**
     * 任务状态
     * @see TaskStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TaskStatus status;

    /**
     * 错误信息（失败时记录）
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;


    /**
     * 任务开始执行时间
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * 任务完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 任务重试次数
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    @Column(name = "max_retry", nullable = false)
    @Builder.Default
    private Integer maxRetry = 3;

    /**
     * 任务超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    @Builder.Default
    private Integer timeoutSeconds = 300;

    /**
     * 队列类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", length = 20, nullable = false)
    @Builder.Default
    private QueueType queueType = QueueType.PRIORITY;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        /** 待处理 */
        PENDING,
        /** 队列中等待 */
        QUEUED,
        /** 执行中 */
        RUNNING,
        /** 已完成 */
        COMPLETED,
        /** 执行失败 */
        FAILED,
        /** 已超时 */
        TIMEOUT,
        /** 已取消 */
        CANCELLED
    }

    /**
     * 判断任务是否为终态
     *
     * @return 是否为终态
     */
    public boolean isTerminalState() {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.FAILED
                || status == TaskStatus.CANCELLED;
    }

    /**
     * 判断任务是否可以重试
     *
     * @return 是否可以重试
     */
    public boolean canRetry() {
        return retryCount < maxRetry && status == TaskStatus.FAILED;
    }
}