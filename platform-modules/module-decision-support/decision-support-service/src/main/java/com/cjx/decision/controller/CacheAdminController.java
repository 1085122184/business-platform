package com.cjx.decision.controller;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.result.Result;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.dto.cache.CacheInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "缓存管理", description = "查看和清理本地 Caffeine 缓存")
public class CacheAdminController {

    private final CaffeineCacheService caffeineCacheService;

    @Operation(summary = "查看全部缓存统计")
    @GetMapping("/stats")
    public Result<List<CacheInfoDTO>> stats(
            @RequestParam(defaultValue = "false") boolean includeKeys,
            @RequestParam(defaultValue = "100") int keyLimit) {
        List<CacheInfoDTO> list = Arrays.stream(CacheType.values())
                .map(type -> buildInfo(type, includeKeys, keyLimit))
                .toList();
        return Result.success(list);
    }

    @Operation(summary = "查看指定缓存统计")
    @GetMapping("/stats/type")
    public Result<CacheInfoDTO> statsByType(
            @RequestParam CacheType type,
            @RequestParam(defaultValue = "true") boolean includeKeys,
            @RequestParam(defaultValue = "100") int keyLimit) {
        return Result.success(buildInfo(type, includeKeys, keyLimit));
    }

    @Operation(summary = "查看指定缓存 key 内容")
    @GetMapping("/value")
    public Result<Object> value(@RequestParam CacheType type, @RequestParam String key) {
        return Result.success(caffeineCacheService.getValue(type, key));
    }

    @Operation(summary = "清理指定缓存 key")
    @DeleteMapping("/key")
    public Result<Boolean> clearKey(@RequestParam CacheType type, @RequestParam String key) {
        caffeineCacheService.remove(type, key);
        return Result.success(true);
    }

    @Operation(summary = "清理指定缓存类型")
    @DeleteMapping("/type")
    public Result<Boolean> clearType(@RequestParam CacheType type) {
        caffeineCacheService.clear(type);
        return Result.success(true);
    }

    @Operation(summary = "清理全部缓存")
    @DeleteMapping("/all")
    public Result<Boolean> clearAll() {
        Arrays.stream(CacheType.values()).forEach(caffeineCacheService::clear);
        return Result.success(true);
    }

    private CacheInfoDTO buildInfo(CacheType type, boolean includeKeys, int keyLimit) {
        CacheInfoDTO dto = new CacheInfoDTO();
        dto.setName(type.name());
        dto.setCacheName(type.getCacheName());
        dto.setMaximumSize(type.getMaximumSize());
        dto.setTtlSeconds(type.getExpireAfterWriteSeconds());
        dto.setEstimatedSize(caffeineCacheService.getEstimatedSize(type));
        dto.setHitCount(caffeineCacheService.getHitCount(type));
        dto.setMissCount(caffeineCacheService.getMissCount(type));
        dto.setLoadSuccessCount(caffeineCacheService.getLoadSuccessCount(type));
        dto.setEvictionCount(caffeineCacheService.getEvictionCount(type));
        dto.setHitRate(caffeineCacheService.getHitRate(type));
        if (includeKeys) {
            dto.setKeys(caffeineCacheService.listKeys(type, keyLimit));
        }
        return dto;
    }
}
