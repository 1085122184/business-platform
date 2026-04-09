package com.cjx.common.core.manager;

import com.cjx.common.core.enums.CacheType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 多策略 Caffeine 缓存管理器
 * <p>按 {@link CacheType} 枚举初始化独立 Cache 实例，每种缓存拥有独立的大小与过期策略</p>
 *
 * @author cuijixu
 * @since 1.0.0
 */
@Slf4j
@Component
public class CaffeineCacheManager {
    /** 缓存容器：cacheType.name -> Cache 实例 */
    private final Map<String, Cache<String, Object>> cacheMap = new ConcurrentHashMap<>(16);

    /**
     * 初始化所有缓存实例
     */
    @PostConstruct
    public void init() {
        for (CacheType type : CacheType.values()) {
            Cache<String, Object> cache = Caffeine.newBuilder()
                    .maximumSize(type.getMaximumSize())
                    .expireAfterWrite(type.getExpireAfterWriteSeconds(), TimeUnit.SECONDS)
                    .recordStats()
                    .build();
            cacheMap.put(type.name(), cache);
            log.info("[CacheManager] 初始化缓存: name={}, maxSize={}, ttl={}s",
                    type.getCacheName(), type.getMaximumSize(), type.getExpireAfterWriteSeconds());
        }
    }

    /**
     * 获取指定类型的 Cache 实例
     *
     * @param cacheType 缓存类型（非空）
     * @return Cache 实例
     */
    public Cache<String, Object> getCache(CacheType cacheType) {
        Objects.requireNonNull(cacheType, "cacheType 不能为空");
        return cacheMap.get(cacheType.name());
    }

    /**
     * 获取指定缓存的统计信息
     *
     * @param cacheType 缓存类型
     * @return CacheStats
     */
    public CacheStats getStats(CacheType cacheType) {
        return getCache(cacheType).stats();
    }
}
