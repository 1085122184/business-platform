package com.cjx.common.strategy.registry;

import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.strategy.annotation.Strategy;
import com.cjx.common.strategy.metadata.StrategyMetadata;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 策略注册表
 * 负责策略的扫描、注册、查找
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class StrategyRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 策略元数据缓存
     * Key: strategyKey (如: user:create, order:pay)
     * Value: StrategyMetadata
     */
    private final Map<String, StrategyMetadata> strategyCache = new ConcurrentHashMap<>(128);

    /**
     * Bean实例缓存
     * Key: beanName
     * Value: Bean实例
     */
    private final Map<String, Object> beanCache = new ConcurrentHashMap<>(64);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化：扫描并注册所有策略
     */
    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();
        log.info("开始扫描并注册策略...");

        try {
            scanAndRegisterStrategies();
            long duration = System.currentTimeMillis() - startTime;
            log.info("策略注册完成，共注册 {} 个策略，耗时 {}ms", strategyCache.size(), duration);

            if (log.isDebugEnabled()) {
                strategyCache.forEach((key, metadata) ->
                        log.debug("已注册策略: key={}, class={}, method={}, description={}",
                                key,
                                metadata.getTargetClass().getSimpleName(),
                                metadata.getTargetMethod().getName(),
                                metadata.getDescription())
                );
            }
        } catch (Exception e) {
            log.error("策略注册失败", e);
            throw new BusinessException("STRATEGY_INIT_FAILED", "策略初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 扫描并注册所有策略
     */
    private void scanAndRegisterStrategies() {
        // 获取所有Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                Class<?> targetClass = AopUtils.getTargetClass(bean);

                // 扫描类上的@Strategy注解
                Strategy classStrategy = AnnotationUtils.findAnnotation(targetClass, Strategy.class);

                // 2. 扫描接口上的注解（新增）
                if (classStrategy == null) {
                    for (Class<?> interfaceClass : targetClass.getInterfaces()) {
                        classStrategy = AnnotationUtils.findAnnotation(interfaceClass, Strategy.class);
                        if (classStrategy != null) {
                            break;
                        }
                    }
                }

                // 扫描方法上的@Strategy注解
                Strategy finalClassStrategy = classStrategy;
                ReflectionUtils.doWithMethods(targetClass, method -> {
                    Strategy methodStrategy = AnnotationUtils.findAnnotation(method, Strategy.class);

                    // 如果没找到，尝试从接口方法查找（新增）
                    if (methodStrategy == null) {
                        methodStrategy = findStrategyFromInterface(targetClass, method);
                    }

                    if (methodStrategy != null) {
                        registerStrategy(beanName, bean, targetClass, method, methodStrategy);
                    } else if (finalClassStrategy != null && Modifier.isPublic(method.getModifiers())) {
                        registerStrategy(beanName, bean, targetClass, method, finalClassStrategy);
                    }
                });

            } catch (Exception e) {
                log.warn("处理Bean时发生异常: beanName={}, error={}", beanName, e.getMessage());
            }
        }
    }
    /**
     * 从接口方法查找@Strategy注解（新增方法）
     */
    private Strategy findStrategyFromInterface(Class<?> targetClass, Method method) {
        for (Class<?> interfaceClass : targetClass.getInterfaces()) {
            try {
                Method interfaceMethod = interfaceClass.getMethod(
                        method.getName(),
                        method.getParameterTypes()
                );
                Strategy strategy = AnnotationUtils.findAnnotation(interfaceMethod, Strategy.class);
                if (strategy != null) {
                    return strategy;
                }
            } catch (NoSuchMethodException e) {
                // 接口中没有对应方法，继续查找其他接口
            }
        }
        return null;
    }
    /**
     * 注册单个策略
     */
    private void registerStrategy(String beanName, Object bean, Class<?> targetClass,
                                  Method method, Strategy strategy) {
        String strategyKey = buildStrategyKey(strategy.value(), targetClass, method);

        if (StringUtils.isEmpty(strategyKey)) {
            log.warn("策略Key为空，跳过注册: class={}, method={}",
                    targetClass.getSimpleName(), method.getName());
            return;
        }

        // 检查是否重复
        if (strategyCache.containsKey(strategyKey)) {
            StrategyMetadata existing = strategyCache.get(strategyKey);
            log.warn("检测到重复的策略Key: key={}, existing={}.{}, new={}.{}",
                    strategyKey,
                    existing.getTargetClass().getSimpleName(),
                    existing.getTargetMethod().getName(),
                    targetClass.getSimpleName(),
                    method.getName());
            return;
        }

        // 创建元数据
        StrategyMetadata metadata = StrategyMetadata.builder()
                .strategyKey(strategyKey)
                .description(StringUtils.hasText(strategy.description())
                        ? strategy.description()
                        : buildDefaultDescription(targetClass, method))
                .beanName(beanName)
                .targetClass(targetClass)
                .targetMethod(method)
                .requireAuth(strategy.requireAuth())
                .permissions(strategy.permissions())
                .build();

        // 设置方法可访问
        ReflectionUtils.makeAccessible(method);

        // 缓存策略和Bean
        strategyCache.put(strategyKey, metadata);
        beanCache.putIfAbsent(beanName, bean);

        log.debug("注册策略成功: key={}, bean={}, method={}",
                strategyKey, beanName, method.getName());
    }

    /**
     * 构建策略Key
     */
    private String buildStrategyKey(String annotationValue, Class<?> targetClass, Method method) {
        if (StringUtils.hasText(annotationValue)) {
            return annotationValue;
        }

        // 如果注解value为空，使用 类名:方法名 格式
        String className = targetClass.getSimpleName()
                .replace("Strategy", "")
                .replace("Service", "")
                .toLowerCase();
        String methodName = method.getName();

        return className + ":" + methodName;
    }

    /**
     * 构建默认描述
     */
    private String buildDefaultDescription(Class<?> targetClass, Method method) {
        return String.format("%s.%s",
                targetClass.getSimpleName(),
                method.getName());
    }

    /**
     * 根据策略Key获取元数据
     */
    public StrategyMetadata getMetadata(String strategyKey) {
        if (!StringUtils.hasText(strategyKey)) {
            throw new BusinessException("STRATEGY_KEY_EMPTY", "策略Key不能为空");
        }

        StrategyMetadata metadata = strategyCache.get(strategyKey);
        if (metadata == null) {
            throw new BusinessException("STRATEGY_NOT_FOUND",
                    "策略不存在: " + strategyKey);
        }

        return metadata;
    }

    /**
     * 根据Bean名称获取Bean实例
     */
    public Object getBean(String beanName) {
        return beanCache.get(beanName);
    }

    /**
     * 获取所有已注册的策略Key
     */
    public Set<String> getAllStrategyKeys() {
        return new HashSet<>(strategyCache.keySet());
    }

    /**
     * 获取所有策略元数据
     */
    public List<StrategyMetadata> getAllMetadata() {
        return new ArrayList<>(strategyCache.values());
    }

    /**
     * 根据前缀查找策略
     */
    public List<StrategyMetadata> findByPrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return Collections.emptyList();
        }

        return strategyCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * 检查策略是否存在
     */
    public boolean exists(String strategyKey) {
        return StringUtils.hasText(strategyKey) && strategyCache.containsKey(strategyKey);
    }

    /**
     * 清空缓存（仅用于测试）
     */
    public void clearCache() {
        strategyCache.clear();
        beanCache.clear();
        log.warn("策略缓存已清空");
    }
}
