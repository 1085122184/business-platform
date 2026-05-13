package com.cjx.uibot.api.vo.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorStatsResponse {
    /**
     * 队列大小
     */
    private Integer queueSize;

    /**
     * 优先级队列大小
     */
    private Integer priorityQueueSize;

    /**
     * 普通队列大小
     */
    private Integer normalQueueSize;

    /**
     * 待处理任务数
     */
    private Long pendingCount;

    /**
     * 队列中任务数
     */
    private Long queuedCount;

    /**
     * 执行中任务数
     */
    private Long runningCount;

    /**
     * 已完成任务数
     */
    private Long completedCount;

    /**
     * 失败任务数
     */
    private Long failedCount;

    /**
     * 超时任务数
     */
    private Long timeoutCount;

    /**
     * 当前执行任务ID
     */
    private String currentTaskId;

    /**
     * 时间戳
     */
    private Long timestamp;
}
