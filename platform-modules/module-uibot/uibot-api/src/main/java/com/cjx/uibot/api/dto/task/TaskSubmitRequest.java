package com.cjx.uibot.api.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务提交请求")
public class TaskSubmitRequest {
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200")
    @Schema(description = "任务名称", example = "流程机器人任务")
    private String taskName;

    @Schema(description = "任务数据（JSON格式）", example = "{\"triggerName\":\"触发器名称\",\"type\":\"uiBot-start\",\"tableName\":\"表名\",\"tableId\":\"表主键id\"}")
    private String taskData;

    @Min(value = 1, message = "优先级最小值为1")
    @Max(value = 10, message = "优先级最大值为10")
    @Schema(description = "任务优先级（1-10，数字越小优先级越高）", example = "5")
    @Builder.Default
    private Integer priority = 6;

    @Schema(description = "队列类型（PRIORITY-优先级队列，NORMAL-普通队列）", example = "NORMAL")
    @Builder.Default
    private String queueType = "NORMAL";
}
