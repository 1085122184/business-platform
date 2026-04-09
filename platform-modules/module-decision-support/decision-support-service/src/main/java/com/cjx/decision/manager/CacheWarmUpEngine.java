package com.cjx.decision.manager;

import com.cjx.decision.annotation.AutoWarmUp;
import com.cjx.decision.event.CacheWarmUpEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动缓存预热核心引擎
 * * @author cuijixu
 */
@Slf4j
@Component
public class CacheWarmUpEngine {
    private final ApplicationContext applicationContext;
    private final List<WarmUpTask> warmUpTasks = new ArrayList<>();

    public CacheWarmUpEngine(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Spring 容器启动完成时执行：扫描所有 Bean，收集打了 @AutoWarmUp 的方法
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initScanner() {
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            Class<?> type = applicationContext.getType(beanName);
            if (type != null && !type.getName().startsWith("org.springframework")) {
                ReflectionUtils.doWithMethods(type, method -> {
                    if (AnnotationUtils.findAnnotation(method, AutoWarmUp.class) != null) {
                        if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(LocalDate.class)) {
                            Object bean = applicationContext.getBean(beanName);
                            warmUpTasks.add(new WarmUpTask(bean, method));
                            log.info("[预热引擎] 成功注册自动预热任务: {}.{}", type.getSimpleName(), method.getName());
                        } else {
                            log.error("[预热引擎] 注册失败！@AutoWarmUp 标注的方法必须且只能包含一个 LocalDate 参数: {}.{}", type.getSimpleName(), method.getName());
                        }
                    }
                });
            }
        }
    }

    /**
     * 监听定时任务发出的广播事件并执行预热
     */
    @Async
    @EventListener
    public void executeAll(CacheWarmUpEvent event) {
        LocalDate date = event.getTargetDate();
        log.info("========== [预热引擎] 收到指令，开始执行预热队列, 目标日期: {}, 任务总数: {} ==========", date, warmUpTasks.size());

        for (WarmUpTask task : warmUpTasks) {
            try {
                ReflectionUtils.makeAccessible(task.method);
                task.method.invoke(task.bean, date);
                log.debug("[预热引擎] 执行完毕: {}", task.method.getName());
            } catch (Exception e) {
                log.error("[预热引擎] 执行异常: {}", task.method.getName(), e);
            }
        }
        log.info("========== [预热引擎] 全局自动预热队列执行完毕 ==========");
    }

    private static class WarmUpTask {
        final Object bean;
        final Method method;
        WarmUpTask(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }
    }
}
