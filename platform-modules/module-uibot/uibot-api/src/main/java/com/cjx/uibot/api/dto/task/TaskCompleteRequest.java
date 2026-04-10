package com.cjx.uibot.api.dto.task;

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
public class TaskCompleteRequest {
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /**
     * 任务是否执行成功
     */
    @NotNull(message = "执行结果不能为空")
    private Boolean success;

    /**
     * 错误信息（失败时填写）
     */
    private String errorMessage;
}
