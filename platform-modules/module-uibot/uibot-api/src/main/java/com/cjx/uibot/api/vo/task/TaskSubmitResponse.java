package com.cjx.uibot.api.vo.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务提交响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务提交响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSubmitResponse {
    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "状态", example = "executing/queued")
    private String status;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "是否在队列中")
    private Boolean queued;

    @Schema(description = "队列位置（如果在队列中）")
    private Integer queuePosition;

    @Schema(description = "时间戳")
    private Long timestamp;
}
