package com.cjx.uibot.api.vo.task;

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
public class TaskCallbackResponse {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 时间戳
     */
    private Long timestamp;
}
