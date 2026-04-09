package com.cjx.uibot.api.dto.task;

import com.cjx.uibot.api.enums.QueueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 队列任务包装类
 * 轻量级对象，用于在优先级队列中排序和管理任务
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTask implements Comparable<QueueTask> {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务优先级（1-10，数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 任务创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 任务入队时间
     */
    private LocalDateTime enqueuedAt;

    /**
     * 队列类型
     */
    private QueueType queueType;

    /**
     * 比较方法，用于优先级队列排序
     *
     * 排序规则：
     * 1. 优先级数字越小，优先级越高
     * 2. 优先级相同时，创建时间越早，优先级越高
     *
     * @param other 另一个任务
     * @return 比较结果
     */
    @Override
    public int compareTo(QueueTask other) {
        if (other == null) {
            return -1;
        }

        // 首先按优先级排序（升序）
        int priorityCompare = this.priority.compareTo(other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        // 优先级相同时，按创建时间排序（升序，早创建的优先）
        if (this.createdAt != null && other.createdAt != null) {
            return this.createdAt.compareTo(other.createdAt);
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueTask queueTask = (QueueTask) o;
        return Objects.equals(taskId, queueTask.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}
