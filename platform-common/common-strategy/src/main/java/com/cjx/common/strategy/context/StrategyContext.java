package com.cjx.common.strategy.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 策略执行上下文
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyContext  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务参数
     */
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 当前用户ID
     */
//    private String userId;

    /**
     * 当前用户名
     */
//    private String username;

    /**
     * 租户ID（多租户场景）
     */
//    private String tenantId;

    /**
     * 客户端IP
     */
//    private String clientIp;

    /**
     * 请求时间戳
     */
    @Builder.Default
    private Long requestTime = System.currentTimeMillis();

    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> extras = new HashMap<>();

    /**
     * 获取参数（类型安全）
     */
    @JsonIgnore
    public <T> Optional<T> getParam(String key, Class<T> clazz) {
        if (params == null || !params.containsKey(key)) {
            return Optional.empty();
        }

        Object value = params.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (clazz.isInstance(value)) {
            return Optional.of(clazz.cast(value));
        }

        // 尝试类型转换
        try {
            if (clazz == String.class) {
                return Optional.of(clazz.cast(String.valueOf(value)));
            }
            if (clazz == Integer.class && value instanceof Number) {
                return Optional.of(clazz.cast(((Number) value).intValue()));
            }
            if (clazz == Long.class && value instanceof Number) {
                return Optional.of(clazz.cast(((Number) value).longValue()));
            }
        } catch (Exception e) {
            // 转换失败，返回empty
        }

        return Optional.empty();
    }

    /**
     * 获取参数（带默认值）
     */
    @JsonIgnore
    public <T> T getParamOrDefault(String key, Class<T> clazz, T defaultValue) {
        return getParam(key, clazz).orElse(defaultValue);
    }

    /**
     * 获取必需参数（不存在则抛异常）
     */
    @JsonIgnore
    public <T> T getRequiredParam(String key, Class<T> clazz) {
        return getParam(key, clazz)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("必需参数缺失: key=%s, type=%s", key, clazz.getSimpleName())
                ));
    }

    /**
     * 设置参数
     */
    public void setParam(String key, Object value) {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
    }

    /**
     * 设置扩展属性
     */
    public void setExtra(String key, Object value) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        this.extras.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    @JsonIgnore
    public <T> Optional<T> getExtra(String key, Class<T> clazz) {
        if (extras == null || !extras.containsKey(key)) {
            return Optional.empty();
        }
        Object value = extras.get(key);
        return value != null && clazz.isInstance(value)
                ? Optional.of(clazz.cast(value))
                : Optional.empty();
    }
}
