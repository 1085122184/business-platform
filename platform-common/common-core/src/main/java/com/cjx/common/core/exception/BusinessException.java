package com.cjx.common.core.exception;

import lombok.Getter;

/**
 * 业务异常
 * @author system
 */
@Getter
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "600";
        this.message = message;
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
