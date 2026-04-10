package com.cjx.uibot.api.dto.task;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务提交请求
 * @author cjx
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmitRequest {
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200")
    private String taskName;

    /**
     * 任务数据（JSON格式）
     */
    private String taskData;

    /**
     * 任务优先级（1-10，数字越小优先级越高）
     */
    @Min(value = 1, message = "优先级最小值为1")
    @Max(value = 10, message = "优先级最大值为10")
    @Builder.Default
    private Integer priority = 6;

    /**
     * 队列类型（PRIORITY-优先级队列，NORMAL-普通队列）
     */
    @Builder.Default
    private String queueType = "NORMAL";
}
