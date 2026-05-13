package com.cjx.common.strategy.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 策略元数据
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyMetadata {

    /**
     * 策略唯一标识
     */
    private String strategyKey;

    /**
     * 策略描述
     */
    private String description;

    /**
     * Bean名称
     */
    private String beanName;

    /**
     * 目标类
     */
    private Class<?> targetClass;

    /**
     * 目标方法
     */
    private Method targetMethod;

    /**
     * 是否需要认证
     */
    private boolean requireAuth;

    /**
     * 所需权限
     */
    private String[] permissions;

    /**
     * 创建时间
     */
    @Builder.Default
    private Long createTime = System.currentTimeMillis();
}
