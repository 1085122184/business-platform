package com.cjx.common.core.manager;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 本地通用缓存管理器
 * 支持过期时间、自动清理
 * @author cuijixu
 */
@Slf4j
@Component
public class MapCacheManager {
    /**
     * 缓存数据存储
     */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 定时清理任务的调度器
     */
    private final ScheduledExecutorService scheduler;

    public MapCacheManager() {
        // 每分钟清理一次过期缓存
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "cache-cleaner");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::cleanExpiredCache, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 缓存实体类
     */
    private static class CacheEntry {
        private final Object value;
        private final long expireTime;

        public CacheEntry(Object value, long ttl) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + ttl;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public Object getValue() {
            return value;
        }

        public long getRemainTtl() {
            return Math.max(0, expireTime - System.currentTimeMillis());
        }
    }

    /**
     * 存入缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间（毫秒）
     */
    public void put(String key, Object value, long ttl) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }
        cache.put(key, new CacheEntry(value, ttl));
        log.debug("缓存已存入: key={}, ttl={}ms", key, ttl);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期返回null
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            log.debug("缓存未命中: key={}", key);
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            log.debug("缓存已过期: key={}", key);
            return null;
        }

        log.debug("缓存命中: key={}, 剩余时间={}ms", key, entry.getRemainTtl());
        return entry.getValue();
    }

    /**
     * 获取缓存（泛型方法）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.error("缓存类型转换失败: key={}, expectedType={}, actualType={}",
                    key, type.getName(), value.getClass().getName());
            return null;
        }
    }

    /**
     * 获取缓存，如果不存在则使用提供的supplier加载
     */
    public <T> T getOrLoad(String key, Class<T> type, long ttl, CacheLoader<T> loader) throws Exception {
        T value = get(key, type);
        if (value != null) {
            return value;
        }

        // 加载数据
        value = loader.load();
        if (value != null) {
            put(key, value, ttl);
        }
        return value;
    }

    /**
     * 删除缓存
     */
    public void remove(String key) {
        cache.remove(key);
        log.debug("缓存已删除: key={}", key);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.clear();
        log.info("所有缓存已清空");
    }

    /**
     * 检查缓存是否存在且未过期
     */
    public boolean exists(String key) {
        CacheEntry entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return cache.size();
    }

    /**
     * 获取缓存剩余时间（毫秒）
     */
    public long getRemainTtl(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            return -1;
        }
        return entry.getRemainTtl();
    }

    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        int cleanedCount = 0;
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                cleanedCount++;
            }
        }
        if (cleanedCount > 0) {
            log.info("清理过期缓存: 清理数量={}, 剩余数量={}", cleanedCount, cache.size());
        }
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("MapCacheManager已关闭");
        }
        cache.clear();
    }

    /**
     * 缓存加载器接口
     */
    @FunctionalInterface
    public interface CacheLoader<T> {
        T load() throws Exception;
    }
}
