package com.cjx.common.web.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 基于Redis + Lua脚本实现
 *
 * 使用示例:
 * @RateLimit(key = "user:login", limit = 5, window = 60)
 * public Result login(LoginRequest request) { ... }
 *
 * @author system
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流key前缀
     * 实际key为: rate:limit:{key}:{ip或userId}
     */
    String key() default "";

    /**
     * 限流次数
     */
    int limit() default 100;

    /**
     * 时间窗口(秒)
     */
    long window() default 60;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.IP;

    enum LimitType {
        /** 根据IP限流 */
        IP,
        /** 根据用户ID限流 */
        USER
    }
}
