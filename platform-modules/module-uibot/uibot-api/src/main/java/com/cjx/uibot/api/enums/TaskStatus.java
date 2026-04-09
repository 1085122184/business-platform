package com.cjx.uibot.api.enums;


import lombok.Getter;

/**
 * 任务状态枚举
 * @author system
 * @since 1.0
 */
@Getter
public enum TaskStatus {
    WAITING("等待中"),
    PROCESSING("处理中"),
    WAITING_CALLBACK("等待外部回调"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }
}