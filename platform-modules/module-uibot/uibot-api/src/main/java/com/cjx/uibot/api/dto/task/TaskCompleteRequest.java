package com.cjx.uibot.api.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务完成请求
 * @author cjx
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务完成回调请求")
public class TaskCompleteRequest {
    @NotBlank(message = "任务ID不能为空")
    @Schema(description = "任务ID", required = true)
    private String taskId;

    @NotNull(message = "执行结果不能为空")
    @Schema(description = "任务是否执行成功", required = true, example = "true")
    private Boolean success;

    @Schema(description = "错误信息（失败时填写）")
    private String errorMessage;
}
