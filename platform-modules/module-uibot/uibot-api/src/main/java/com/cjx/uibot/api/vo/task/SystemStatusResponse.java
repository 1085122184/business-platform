package com.cjx.uibot.api.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统状态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统状态")
public class SystemStatusResponse {
    @Schema(description = "队列大小")
    private Integer queueSize;

    @Schema(description = "优先级队列大小")
    private Integer priorityQueueSize;

    @Schema(description = "普通队列大小")
    private Integer normalQueueSize;

    @Schema(description = "当前执行任务ID")
    private String currentTaskId;

    @Schema(description = "时间戳")
    private Long timestamp;
}
