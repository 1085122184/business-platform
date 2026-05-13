package com.cjx.common.web.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池管理配置类
 * 符合阿里巴巴Java开发规范，提供企业级线程池管理
 *
 * @author cui
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Value("${thread.pool.core-size:10}")
    private int corePoolSize;

    @Value("${thread.pool.max-size:20}")
    private int maxPoolSize;

    @Value("${thread.pool.queue-capacity:200}")
    private int queueCapacity;

    @Value("${thread.pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${thread.pool.await-termination-seconds:60}")
    private int awaitTerminationSeconds;

    /**
     * 异步任务线程池
     * 用于@Async注解的异步方法执行
     */
    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程名称前缀
        executor.setThreadNamePrefix("async-executor-");

        // 拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // 初始化
        executor.initialize();

        log.info("异步线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    /**
     * IO密集型任务线程池
     * 适用于文件读写、网络请求等IO操作
     */
    @Bean(name = "ioExecutor")
    public ThreadPoolTaskExecutor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // IO密集型：线程数 = CPU核心数 * 2
        int processors = Runtime.getRuntime().availableProcessors();
        int ioThreads = processors * 2;

        executor.setCorePoolSize(ioThreads);
        executor.setMaxPoolSize(ioThreads * 2);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("io-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        executor.initialize();

        log.info("IO线程池初始化完成 - 核心线程数: {}, 最大线程数: {}", ioThreads, ioThreads * 2);

        return executor;
    }

    /**
     * CPU密集型任务线程池
     * 适用于复杂计算、数据处理等CPU操作
     */
    @Bean(name = "cpuExecutor")
    public ThreadPoolTaskExecutor cpuExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // CPU密集型：线程数 = CPU核心数 + 1
        int processors = Runtime.getRuntime().availableProcessors();
        int cpuThreads = processors + 1;

        executor.setCorePoolSize(cpuThreads);
        executor.setMaxPoolSize(cpuThreads);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("cpu-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        executor.initialize();

        log.info("CPU线程池初始化完成 - 核心线程数: {}, 最大线程数: {}", cpuThreads, cpuThreads);

        return executor;
    }

    /**
     * 定时任务线程池
     * 用于执行定时调度任务
     */
    @Bean(name = "scheduledExecutor")
    public ScheduledThreadPoolExecutor scheduledExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                processors,
                new CustomThreadFactory("scheduled-executor"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 设置在关闭时继续执行现有的延迟任务
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        // 设置在关闭时取消周期任务
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);

        log.info("定时任务线程池初始化完成 - 核心线程数: {}", processors);

        return executor;
    }

    /**
     * 自定义线程工厂
     * 提供有意义的线程名称，便于问题排查
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final ThreadGroup group;

        CustomThreadFactory(String namePrefix) {
            this.group = Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

            // 设置为非守护线程
            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            // 设置线程优先级
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            // 设置未捕获异常处理器
            t.setUncaughtExceptionHandler((thread, throwable) ->
                    log.error("线程 {} 发生未捕获异常", thread.getName(), throwable)
            );

            return t;
        }
    }

    /**
     * 线程池监控和优雅关闭
     */
    @PreDestroy
    public void destroy() {
        log.info("开始关闭线程池...");
        // Spring会自动调用线程池的shutdown方法
        log.info("线程池关闭完成");
    }
}
