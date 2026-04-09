package com.cjx.common.strategy.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * 策略执行结果
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 结果数据
     */
    private T data;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 时间戳
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    /**
     * 创建成功结果
     */
    public static <T> StrategyResult<T> success(T data) {
        return StrategyResult.<T>builder()
                .success(true)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建成功结果（无数据）
     */
    public static <T> StrategyResult<T> success() {
        return success(null);
    }

    /**
     * 创建失败结果
     */
    public static <T> StrategyResult<T> failure(String errorCode, String errorMessage) {
        return StrategyResult.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败结果（系统错误）
     */
    public static <T> StrategyResult<T> systemError(String message) {
        return failure("SYSTEM_ERROR", message);
    }

    /**
     * 创建失败结果（业务错误）
     */
    public static <T> StrategyResult<T> businessError(String errorCode, String message) {
        return failure(errorCode, message);
    }
}
