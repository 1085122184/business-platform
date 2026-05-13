package com.cjx.common.ai.exception;

/**
 * AI 模块统一运行时异常
 */
public class AiException extends RuntimeException {

    private final int code;

    public AiException(String message) {
        super(message);
        this.code = 500;
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public AiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
