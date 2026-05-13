package com.cjx.uibot.manager;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ThreadPoolManager {
    private final ExecutorService asyncExecutor;

    public ThreadPoolManager() {
        // 创建定长线程池
        this.asyncExecutor = Executors.newFixedThreadPool(10, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "async-thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public void executeAsync(Runnable task) {
        asyncExecutor.execute(task);
    }

    public <T> CompletableFuture<T> submitAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }

    @PreDestroy
    public void shutdown() {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
