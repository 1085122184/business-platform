package com.cjx.uibot.config;

import com.cjx.uibot.api.dto.task.QueueTask;
import com.cjx.uibot.entity.UiBotProcess;
import com.cjx.uibot.entity.UiBotProcessStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 双队列配置类
 * 同时提供优先级队列和普通队列两种队列类型
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Configuration
public class QueueConfig {
    /**
     * 创建uiBot任务队列 Bean
     */
    @Bean
    public Queue<UiBotProcess> uiBotQueue() {
        return new ConcurrentLinkedQueue<>();
    }


    /**
     * 流程运行状态
     */
    @Bean
    public Map<String, UiBotProcessStatus> statusCache() {
        return new ConcurrentHashMap<>();
    }



    /**
     * 创建优先级阻塞队列Bean
     * 使用PriorityBlockingQueue确保线程安全和按优先级自动排序
     *
     * 特点：
     * - 按优先级排序（priority字段）
     * - 相同优先级按创建时间排序
     * - 无界队列，自动扩容
     * - 线程安全
     *
     * @return 优先级阻塞队列
     */
    @Bean("priorityTaskQueue")
    public PriorityBlockingQueue<QueueTask> priorityTaskQueue() {
        // 初始容量100，可自动扩容
        return new PriorityBlockingQueue<>(100);
    }

    /**
     * 创建普通阻塞队列Bean（FIFO）
     * 使用LinkedBlockingQueue实现先进先出
     *
     * 特点：
     * - 先进先出（FIFO）
     * - 严格按照提交时间顺序执行
     * - 不考虑优先级
     * - 线程安全
     * - 有界队列，防止内存溢出
     *
     * @return 普通阻塞队列
     */
    @Bean("normalTaskQueue")
    public LinkedBlockingQueue<QueueTask> normalTaskQueue() {
        // 容量10000，防止无限增长
        return new LinkedBlockingQueue<>(10000);
    }

}
