package com.cjx.common.core.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于处理业务逻辑中的异常情况，包含错误码和错误信息
 *
 * @author system
 */
@Getter
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造业务异常
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "600";
        this.message = message;
    }

    /**
     * 构造业务异常
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 构造业务异常（带原始异常）
     * @param errorCode 错误码
     * @param message 错误信息
     * @param cause 原始异常
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 构造业务异常（带原始异常）
     * @param message 错误信息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "600";
        this.message = message;
    }
}
