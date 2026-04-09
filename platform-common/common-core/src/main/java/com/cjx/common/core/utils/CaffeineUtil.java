package com.cjx.common.core.utils;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.manager.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Caffeine 缓存工具类
 * @author cuijixu
 * @since 1.0.0
 */
@Slf4j
@Component
public class CaffeineUtil {
    private final CaffeineCacheManager cacheManager;
    private static final Object NULL_PLACEHOLDER = new Object();

    public CaffeineUtil(CaffeineCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // ===================== 基本 CRUD =====================

    /**
     * 存入缓存
     *
     * @param cacheType 缓存类型
     * @param key       缓存键（非空）
     * @param value     缓存值（非空）
     */
    public void put(CacheType cacheType, String key, Object value) {
        checkParams(cacheType, key);
        Assert.notNull(value, "缓存 value 不能为 null");
        getCache(cacheType).put(key, value);
        log.debug("[Cache] put -> type={}, key={}", cacheType.getCacheName(), key);
    }

    /**
     * 获取缓存（不存在时返回 null，不触发回源）
     *
     * @param cacheType 缓存类型
     * @param key       缓存键
     * @param clazz     目标类型
     * @param <T>       值类型
     * @return 缓存值，不存在返回 null
     */
    public <T> T get(CacheType cacheType, String key, Class<T> clazz) {
        checkParams(cacheType, key);
        Object value = getCache(cacheType).getIfPresent(key);

        // 🌟 2. 如果拿到的是占位符，说明之前查过数据库且为空，直接拦截并返回 null 给业务层
        if (value == NULL_PLACEHOLDER) {
            log.debug("[Cache] hit null placeholder -> type={}, key={}", cacheType.getCacheName(), key);
            return null;
        }

        if (value == null) {
            log.debug("[Cache] miss -> type={}, key={}", cacheType.getCacheName(), key);
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 删除指定缓存键
     *
     * @param cacheType 缓存类型
     * @param key       缓存键
     */
    public void remove(CacheType cacheType, String key) {
        checkParams(cacheType, key);
        getCache(cacheType).invalidate(key);
        log.debug("[Cache] remove -> type={}, key={}", cacheType.getCacheName(), key);
    }

    /**
     * 清空指定类型的全部缓存
     *
     * @param cacheType 缓存类型
     */
    public void clear(CacheType cacheType) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        getCache(cacheType).invalidateAll();
        log.info("[Cache] clear all -> type={}", cacheType.getCacheName());
    }

    /**
     * 判断缓存键是否存在
     *
     * @param cacheType 缓存类型
     * @param key       缓存键
     * @return true 表示存在
     */
    public boolean exists(CacheType cacheType, String key) {
        checkParams(cacheType, key);
        return getCache(cacheType).getIfPresent(key) != null;
    }

    // ===================== 回源加载（Loading Cache）=====================

    /**
     * 获取缓存，不存在则通过 loader 回源加载并写入缓存
     * <p>线程安全，同一 key 并发时只有一个线程执行 loader，其他线程等待结果</p>
     *
     * @param cacheType 缓存类型
     * @param key       缓存键
     * @param loader    回源函数（入参为 key，返回值为缓存值）
     * @param clazz     目标类型
     * @param <T>       值类型
     * @return 缓存值（可能为 null，若 loader 返回 null 则不写入缓存）
     */
    public <T> T getOrLoad(CacheType cacheType, String key, Function<String, T> loader, Class<T> clazz) {
        checkParams(cacheType, key);
        Assert.notNull(loader, "loader 不能为空");

        Object cached = getCache(cacheType).get(key, k -> {
            log.debug("[Cache] load from source -> type={}, key={}", cacheType.getCacheName(), k);
            T result = loader.apply(k);
            if (result == null) {
                log.warn("[Cache] loader 返回 null，缓存占位符防穿透，key={}", k);
                return NULL_PLACEHOLDER;
            }
            return result;
        });

        if (cached == NULL_PLACEHOLDER) {
            return null;
        }
        return clazz.cast(cached);
    }

    public <T> List<T> getOrLoadList(CacheType cacheType, String key, Function<String, List<T>> loader) {
        checkParams(cacheType, key);
        Object cached = getCache(cacheType).get(key, k -> {
            List<T> result = loader.apply(k);
            // 如果是 null，转换为安全的空集合（防止 NullPointerException），并进行缓存
            if (result == null) {
                log.warn("[Cache] loader 返回 null，自动转化为空集合进行缓存，key={}", k);
                return java.util.Collections.emptyList();
            }
            return result; // 即使是空的 ArrayList，也正常返回并让 Caffeine 缓存它！
        });

        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) cached;
        return result;
    }
    // ===================== 条件操作 =====================

    /**
     * 仅当 key 不存在时写入（原子操作，防重复写）
     *
     * @param cacheType 缓存类型
     * @param key       缓存键
     * @param value     缓存值
     * @return true 表示写入成功，false 表示 key 已存在
     */
    public boolean putIfAbsent(CacheType cacheType, String key, Object value) {
        checkParams(cacheType, key);
        Assert.notNull(value, "缓存 value 不能为 null");
        Cache<String, Object> cache = getCache(cacheType);
        // asMap().putIfAbsent 是原子操作
        Object existing = cache.asMap().putIfAbsent(key, value);
        boolean inserted = existing == null;
        log.debug("[Cache] putIfAbsent -> type={}, key={}, inserted={}", cacheType.getCacheName(), key, inserted);
        return inserted;
    }

    // ===================== 批量操作 =====================

    /**
     * 批量写入缓存
     *
     * @param cacheType 缓存类型
     * @param dataMap   键值对（非空）
     */
    public void putAll(CacheType cacheType, Map<String, Object> dataMap) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        Assert.notEmpty(dataMap, "dataMap 不能为空");
        getCache(cacheType).putAll(dataMap);
        log.debug("[Cache] putAll -> type={}, size={}", cacheType.getCacheName(), dataMap.size());
    }

    /**
     * 批量获取缓存（仅返回已命中的键值对）
     *
     * @param cacheType 缓存类型
     * @param keys      缓存键集合
     * @return 命中的键值 Map
     */
    public Map<String, Object> getAll(CacheType cacheType, Collection<String> keys) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        Assert.notEmpty(keys, "keys 不能为空");
        Map<String, Object> result = getCache(cacheType).getAllPresent(keys);
        log.debug("[Cache] getAll -> type={}, query={}, hit={}", cacheType.getCacheName(), keys.size(), result.size());
        return result;
    }

    /**
     * 批量删除缓存
     *
     * @param cacheType 缓存类型
     * @param keys      缓存键集合
     */
    public void removeAll(CacheType cacheType, Collection<String> keys) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        Assert.notEmpty(keys, "keys 不能为空");
        getCache(cacheType).invalidateAll(keys);
        log.debug("[Cache] removeAll -> type={}, size={}", cacheType.getCacheName(), keys.size());
    }

    // ===================== 异步加载 =====================

    /**
     * 异步加载缓存（适用于非阻塞场景）
     * <p>先返回 CompletableFuture，不阻塞主线程，回调中写入缓存</p>
     *
     * @param cacheType 缓存类型
     * @param key       缓存键
     * @param loader    回源函数（异步执行）
     * @param clazz     目标类型
     * @param <T>       值类型
     * @return CompletableFuture
     */
    public <T> CompletableFuture<T> getAsync(CacheType cacheType, String key,
                                             Function<String, T> loader, Class<T> clazz) {
        checkParams(cacheType, key);
        Assert.notNull(loader, "loader 不能为空");

        Object cached = getCache(cacheType).getIfPresent(key);
        if (cached != null) {
            return CompletableFuture.completedFuture(clazz.cast(cached));
        }

        return CompletableFuture.supplyAsync(() -> {
            T result = loader.apply(key);
            if (result != null) {
                put(cacheType, key, result);
            }
            return result;
        });
    }

    // ===================== 监控统计 =====================

    /**
     * 获取指定缓存的统计信息
     * <p>需在 Caffeine 构建时调用 {@code .recordStats()} 才能生效</p>
     *
     * @param cacheType 缓存类型
     * @return CacheStats
     */
    public CacheStats getStats(CacheType cacheType) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        return cacheManager.getStats(cacheType);
    }

    /**
     * 获取缓存命中率（0.0 ~ 1.0）
     *
     * @param cacheType 缓存类型
     * @return 命中率
     */
    public double getHitRate(CacheType cacheType) {
        return getStats(cacheType).hitRate();
    }

    /**
     * 获取缓存当前估算大小
     *
     * @param cacheType 缓存类型
     * @return 缓存条数（估算值）
     */
    public long getEstimatedSize(CacheType cacheType) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        return getCache(cacheType).estimatedSize();
    }

    /**
     * 打印指定缓存的统计信息到日志（INFO 级别）
     *
     * @param cacheType 缓存类型
     */
    public void printStats(CacheType cacheType) {
        CacheStats stats = getStats(cacheType);
        log.info("[Cache Stats] type={}, hitCount={}, missCount={}, hitRate={}, loadSuccessCount={}, evictionCount={}",
                cacheType.getCacheName(),
                stats.hitCount(),
                stats.missCount(),
                String.format("%.2f%%", stats.hitRate() * 100),
                stats.loadSuccessCount(),
                stats.evictionCount());
    }

    // ===================== 私有方法 =====================

    /**
     * 获取 Cache 实例
     */
    private Cache<String, Object> getCache(CacheType cacheType) {
        return cacheManager.getCache(cacheType);
    }

    /**
     * 参数校验（cacheType 和 key 均不能为空）
     */
    private void checkParams(CacheType cacheType, String key) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        Assert.hasText(key, "缓存 key 不能为空");
    }
}
