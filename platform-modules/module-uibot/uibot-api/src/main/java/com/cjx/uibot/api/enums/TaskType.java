package com.cjx.uibot.api.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 * @author system
 * @since 1.0
 */
@Getter
public enum TaskType {
    USER_TASK("用户任务", "/api/external/user"),
    ORDER_TASK("订单任务", "/api/external/order"),
    PRODUCT_TASK("产品任务", "/api/external/product");

    private final String description;
    private final String defaultPath;

    TaskType(String description, String defaultPath) {
        this.description = description;
        this.defaultPath = defaultPath;
    }
}
