package com.cjx.uibot.api.vo.task;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSubmitResponse {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 状态
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 是否在队列中
     */
    private Boolean queued;

    /**
     * 队列位置（如果在队列中）
     */
    private Integer queuePosition;

    /**
     * 时间戳
     */
    private Long timestamp;
}
