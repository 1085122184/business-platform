package com.cjx.common.redis.utils;

import com.cjx.common.redis.exception.RedisOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 高级封装
 *
 * 提供常用的Redis操作方法，包含: String、Hash、Set、ZSet、List、分布式锁、限流等
 * 写操作异常会抛出RedisOperationException，读操作异常记录日志并返回默认值
 *
 * @author system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    // ========== String类型操作 ==========

    /**
     * 设置缓存值
     * @param key 缓存键
     * @param value 缓存值
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            throw new RedisOperationException("Redis set error", key, "set", e);
        }
    }

    /**
     * 设置缓存值（带过期时间）
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            throw new RedisOperationException("Redis set error", key, "set", e);
        }
    }

    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 缓存值，操作失败返回null
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error, key={}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存值（带异常抛出）
     * @param key 缓存键
     * @return 缓存值
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Object getOrThrow(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new RedisOperationException("Redis get error", key, "get", e);
        }
    }

    /**
     * 删除缓存
     * @param key 缓存键
     * @return 是否删除成功
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RedisOperationException("Redis delete error", key, "delete", e);
        }
    }

    /**
     * 批量删除缓存
     * @param keys 缓存键集合
     * @return 删除数量
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long deleteBatch(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            throw new RedisOperationException("Redis deleteBatch error", "batch", "deleteBatch", e);
        }
    }

    /**
     * 设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            throw new RedisOperationException("Redis expire error", key, "expire", e);
        }
    }

    /**
     * 获取过期时间
     * @param key 缓存键
     * @return 过期时间（秒），-2表示key不存在，-1表示不过期
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis getExpire error, key={}", key, e);
            return -2L;
        }
    }

    /**
     * 判断key是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey error, key={}", key, e);
            return false;
        }
    }

    /**
     * 递增
     * @param key 缓存键
     * @param delta 增量
     * @return 递增后的值
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            throw new RedisOperationException("Redis increment error", key, "increment", e);
        }
    }

    /**
     * 递减
     * @param key 缓存键
     * @param delta 减量
     * @return 递减后的值
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            throw new RedisOperationException("Redis decrement error", key, "decrement", e);
        }
    }

    // ========== Hash类型操作 ==========

    /**
     * 设置Hash字段值
     * @param key 缓存键
     * @param field 字段名
     * @param value 字段值
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            throw new RedisOperationException("Redis hSet error", key, "hSet", e);
        }
    }

    /**
     * 获取Hash字段值
     * @param key 缓存键
     * @param field 字段名
     * @return 字段值
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis hGet error, key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * 批量设置Hash字段
     * @param key 缓存键
     * @param map 字段映射
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public void hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            throw new RedisOperationException("Redis hSetAll error", key, "hSetAll", e);
        }
    }

    /**
     * 获取所有Hash字段
     * @param key 缓存键
     * @return 字段映射
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis hGetAll error, key={}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 删除Hash字段
     * @param key 缓存键
     * @param fields 字段名数组
     * @return 删除数量
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            throw new RedisOperationException("Redis hDelete error", key, "hDelete", e);
        }
    }

    /**
     * 判断Hash字段是否存在
     * @param key 缓存键
     * @param field 字段名
     * @return 是否存在
     */
    public Boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("Redis hHasKey error, key={}, field={}", key, field, e);
            return false;
        }
    }

    /**
     * Hash字段递增
     * @param key 缓存键
     * @param field 字段名
     * @param delta 增量
     * @return 递增后的值
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            throw new RedisOperationException("Redis hIncrement error", key, "hIncrement", e);
        }
    }

    // ========== Set类型操作 ==========

    /**
     * 添加Set成员
     * @param key 缓存键
     * @param values 成员值数组
     * @return 添加数量
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            throw new RedisOperationException("Redis sAdd error", key, "sAdd", e);
        }
    }

    /**
     * 获取Set所有成员
     * @param key 缓存键
     * @return 成员集合
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis sMembers error, key={}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 判断Set成员是否存在
     * @param key 缓存键
     * @param value 成员值
     * @return 是否存在
     */
    public Boolean sIsMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Redis sIsMember error, key={}", key, e);
            return false;
        }
    }

    /**
     * 删除Set成员
     * @param key 缓存键
     * @param values 成员值数组
     * @return 删除数量
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            throw new RedisOperationException("Redis sRemove error", key, "sRemove", e);
        }
    }

    // ========== ZSet类型操作 ==========

    /**
     * 添加ZSet成员
     * @param key 缓存键
     * @param value 成员值
     * @param score 分数
     * @return 是否添加成功
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Boolean zAdd(String key, Object value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            throw new RedisOperationException("Redis zAdd error", key, "zAdd", e);
        }
    }

    /**
     * 按分数范围获取ZSet成员
     * @param key 缓存键
     * @param min 最小分数
     * @param max 最大分数
     * @return 成员集合
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max);
        } catch (Exception e) {
            log.error("Redis zRangeByScore error, key={}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 删除ZSet成员
     * @param key 缓存键
     * @param values 成员值数组
     * @return 删除数量
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Long zRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            throw new RedisOperationException("Redis zRemove error", key, "zRemove", e);
        }
    }

    // ========== 分布式锁(基于Lua脚本) ==========

    /**
     * 获取分布式锁
     * @param lockKey 锁的key
     * @param requestId 请求ID(用于释放锁时校验)
     * @param expireTime 过期时间(秒)
     * @return 是否获取成功
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Boolean tryLock(String lockKey, String requestId, long expireTime) {
        try {
            String script = "return redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2])";
            DefaultRedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);
            String result = redisTemplate.execute(redisScript,
                    Collections.singletonList(lockKey),
                    requestId,
                    String.valueOf(expireTime));
            return "OK".equals(result);
        } catch (Exception e) {
            throw new RedisOperationException("Redis tryLock error", lockKey, "tryLock", e);
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     * @param requestId 请求ID
     * @return 是否释放成功
     * @throws RedisOperationException 当Redis操作失败时抛出
     */
    public Boolean releaseLock(String lockKey, String requestId) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            Long result = redisTemplate.execute(redisScript,
                    Collections.singletonList(lockKey),
                    requestId);
            return result != null && result > 0;
        } catch (Exception e) {
            throw new RedisOperationException("Redis releaseLock error", lockKey, "releaseLock", e);
        }
    }

    // ========== 限流(固定窗口限流) ==========

    /**
     * 限流检查
     * @param key 限流key
     * @param limit 限制次数
     * @param window 时间窗口(秒)
     * @return true:允许访问 false:触发限流
     */
    public Boolean checkRateLimit(String key, int limit, long window) {
        try {
            String script =
                    "local key = KEYS[1] " +
                            "local limit = tonumber(ARGV[1]) " +
                            "local window = tonumber(ARGV[2]) " +
                            "local current = tonumber(redis.call('get', key) or '0') " +
                            "if current < limit then " +
                            "    redis.call('incr', key) " +
                            "    if current == 0 then " +
                            "        redis.call('expire', key, window) " +
                            "    end " +
                            "    return 1 " +
                            "else " +
                            "    return 0 " +
                            "end";

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            Long result = redisTemplate.execute(redisScript,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    String.valueOf(window));

            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Redis checkRateLimit error, key={}", key, e);
            // 限流失败默认允许访问,避免影响正常业务
            return true;
        }
    }

    // ========== Pipeline批量操作 ==========

    /**
     * Pipeline批量获取
     * @param keys 缓存键集合
     * @return 缓存值列表
     */
    public List<Object> batchGet(List<String> keys) {
        try {
            return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String key : keys) {
                    connection.get(key.getBytes());
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Redis batchGet error", e);
            return Collections.emptyList();
        }
    }
}
