package com.cjx.uibot.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    INVALID_PARAMETER(400, "参数错误"),
    METHOD_NOT_FOUND(404, "方法不存在"),
    MODULE_NOT_FOUND(404, "模块不存在"),
    EXTERNAL_API_ERROR(500, "外部API调用失败"),
    SYSTEM_ERROR(500, "系统内部错误"),
    FEIGN_TIMEOUT(504, "外部服务超时"),
    CIRCUIT_BREAKER_OPEN(503, "服务熔断");

    private final Integer code;
    private final String message;
}
