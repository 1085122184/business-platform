package com.cjx.common.core.enums;

import lombok.Getter;

/**
 * 缓存类型枚举
 *
 * @author cuijixu
 * @since 1.0.0
 */
@Getter
public enum CacheType {

    /**
     * 默认缓存：最大 500 条，TTL 5 分钟
     */
    DEFAULT("default", 500, 300),

    /**
     * 用户信息缓存：最大 1000 条，TTL 30 分钟
     */
    USER_INFO("userInfo", 1000, 1800),

    /**
     * 配置/字典缓存：最大 200 条，TTL 1 小时
     */
    CONFIG("config", 200, 3600),

    /**
     * 热点数据缓存：最大 100 条，TTL 1 分钟（高频刷新）
     */
    HOT_DATA("hotData", 100, 60),

    /**
     * 当日数据缓存：最大 5000 条，TTL 10 小时
     */
    TODAY_DATA("todayData", 5000, 36000),

    /**
     * Token 缓存：最大 5000 条，TTL 2 小时
     */
    TOKEN("token", 5000, 7200),

    /**
     * Token 缓存：最大 5000 条，TTL 2 小时
     */
    USER_PERMISSIONS("user_perms", 10000, 3600);

    /** 缓存名称（对应 @Cacheable 的 cacheNames） */
    private final String cacheName;

    /** 最大缓存条数 */
    private final int maximumSize;

    /** 过期时间（写入后，秒） */
    private final long expireAfterWriteSeconds;

    CacheType(String cacheName, int maximumSize, long expireAfterWriteSeconds) {
        this.cacheName = cacheName;
        this.maximumSize = maximumSize;
        this.expireAfterWriteSeconds = expireAfterWriteSeconds;
    }
}
