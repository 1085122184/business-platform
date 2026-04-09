package com.cjx.uibot.api.vo.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务详情响应
 * @author cjx
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务详情")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDetailResponse {
    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务数据")
    private String taskData;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "开始执行时间")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "最大重试次数")
    private Integer maxRetry;

    @Schema(description = "队列类型")
    private String queueType;

}
