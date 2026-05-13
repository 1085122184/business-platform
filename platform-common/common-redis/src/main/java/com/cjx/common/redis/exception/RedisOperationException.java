package com.cjx.common.redis.exception;

/**
 * Redis操作异常
 * 用于封装Redis操作过程中的异常，使调用方能够感知和处理异常
 *
 * @author system
 */
public class RedisOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Redis操作键
     */
    private final String key;

    /**
     * 操作类型
     */
    private final String operation;

    /**
     * 构造Redis操作异常
     * @param message 异常信息
     * @param key Redis键
     * @param operation 操作类型
     * @param cause 原始异常
     */
    public RedisOperationException(String message, String key, String operation, Throwable cause) {
        super(message, cause);
        this.key = key;
        this.operation = operation;
    }

    /**
     * 构造Redis操作异常（无key）
     * @param message 异常信息
     * @param operation 操作类型
     * @param cause 原始异常
     */
    public RedisOperationException(String message, String operation, Throwable cause) {
        super(message, cause);
        this.key = null;
        this.operation = operation;
    }

    public String getKey() {
        return key;
    }

    public String getOperation() {
        return operation;
    }
}
