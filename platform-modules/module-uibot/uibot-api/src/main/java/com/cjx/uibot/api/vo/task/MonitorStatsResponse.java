package com.cjx.uibot.api.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "监控统计数据")
public class MonitorStatsResponse {
    @Schema(description = "队列大小")
    private Integer queueSize;

    @Schema(description = "优先级队列大小")
    private Integer priorityQueueSize;

    @Schema(description = "普通队列大小")
    private Integer normalQueueSize;

    @Schema(description = "待处理任务数")
    private Long pendingCount;

    @Schema(description = "队列中任务数")
    private Long queuedCount;

    @Schema(description = "执行中任务数")
    private Long runningCount;

    @Schema(description = "已完成任务数")
    private Long completedCount;

    @Schema(description = "失败任务数")
    private Long failedCount;

    @Schema(description = "超时任务数")
    private Long timeoutCount;

    @Schema(description = "当前执行任务ID")
    private String currentTaskId;

    @Schema(description = "时间戳")
    private Long timestamp;
}
