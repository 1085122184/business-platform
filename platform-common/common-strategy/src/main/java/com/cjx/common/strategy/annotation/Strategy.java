package com.cjx.common.strategy.annotation;

import java.lang.annotation.*;

/**
 * 策略标识注解
 * 用于标记策略类和方法
 *
 * @author Enterprise Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Strategy {

    /**
     * 策略唯一标识
     * 格式: 业务域:业务类型，如 user:create, order:pay
     */
    String value();

    /**
     * 策略描述
     */
    String description() default "";

    /**
     * 是否需要认证
     */
    boolean requireAuth() default false;

    /**
     * 所需权限
     */
    String[] permissions() default {};
}
