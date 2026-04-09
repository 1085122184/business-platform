package com.cjx.uibot.api.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务回调响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务回调响应")
public class TaskCallbackResponse {
    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "处理状态")
    private String status;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "时间戳")
    private Long timestamp;
}
