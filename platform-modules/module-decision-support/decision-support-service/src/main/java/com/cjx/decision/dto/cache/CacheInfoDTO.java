package com.cjx.decision.dto.cache;

import lombok.Data;

import java.util.List;

@Data
public class CacheInfoDTO {
    private String name;
    private String cacheName;
    private int maximumSize;
    private long ttlSeconds;
    private long estimatedSize;
    private long hitCount;
    private long missCount;
    private long loadSuccessCount;
    private long evictionCount;
    private double hitRate;
    private List<String> keys;
}
