package com.cjx.uibot.api.enums;
/**
 * 队列类型枚举
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
public enum QueueType {
    /**
            * 优先级队列
     * 按照优先级和创建时间排序
     * 优先级数字越小越优先，相同优先级按创建时间排序
     */
    PRIORITY("优先级队列"),

    /**
     * 普通队列（FIFO）
     * 先进先出，按照提交顺序执行
     * 不考虑优先级，严格按照时间顺序
     */
    NORMAL("普通队列");

    private final String description;

    QueueType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
