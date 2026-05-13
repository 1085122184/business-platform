package com.cjx.uibot.api.vo.task;

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
public class SystemStatusResponse {
    /**
     * 队列大小
     */
    private Integer queueSize;

    /**
     * 优先级队列大小
     */
    private Integer priorityQueueSize;

    /**
     * 普通队列大小
     */
    private Integer normalQueueSize;

    /**
     * 当前执行任务ID
     */
    private String currentTaskId;

    /**
     * 时间戳
     */
    private Long timestamp;
}
