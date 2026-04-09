package com.cjx.common.redis.utils;

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
 * 提供常用的Redis操作方法
 * 包含: String、Hash、Set、ZSet、List、分布式锁、限流等
 *
 * @author system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    // ========== String类型操作 ==========

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set error, key={}", key, e);
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis set error, key={}", key, e);
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error, key={}", key, e);
            return null;
        }
    }

    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete error, key={}", key, e);
            return false;
        }
    }

    public Long deleteBatch(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis deleteBatch error", e);
            return 0L;
        }
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("Redis expire error, key={}", key, e);
            return false;
        }
    }

    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis getExpire error, key={}", key, e);
            return -2L;
        }
    }

    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey error, key={}", key, e);
            return false;
        }
    }

    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis increment error, key={}", key, e);
            return null;
        }
    }

    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis decrement error, key={}", key, e);
            return null;
        }
    }

    // ========== Hash类型操作 ==========

    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis hSet error, key={}, field={}", key, field, e);
        }
    }

    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis hGet error, key={}, field={}", key, field, e);
            return null;
        }
    }

    public void hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("Redis hSetAll error, key={}", key, e);
        }
    }

    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis hGetAll error, key={}", key, e);
            return Collections.emptyMap();
        }
    }

    public Long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("Redis hDelete error, key={}", key, e);
            return 0L;
        }
    }

    public Boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("Redis hHasKey error, key={}, field={}", key, field, e);
            return false;
        }
    }

    public Long hIncrement(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Redis hIncrement error, key={}, field={}", key, field, e);
            return null;
        }
    }

    // ========== Set类型操作 ==========

    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis sAdd error, key={}", key, e);
            return 0L;
        }
    }

    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis sMembers error, key={}", key, e);
            return Collections.emptySet();
        }
    }

    public Boolean sIsMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("Redis sIsMember error, key={}", key, e);
            return false;
        }
    }

    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis sRemove error, key={}", key, e);
            return 0L;
        }
    }

    // ========== ZSet类型操作 ==========

    public Boolean zAdd(String key, Object value, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("Redis zAdd error, key={}", key, e);
            return false;
        }
    }

    public Set<Object> zRangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max);
        } catch (Exception e) {
            log.error("Redis zRangeByScore error, key={}", key, e);
            return Collections.emptySet();
        }
    }

    public Long zRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis zRemove error, key={}", key, e);
            return 0L;
        }
    }

    // ========== 分布式锁(基于Lua脚本) ==========

    /**
     * 获取分布式锁
     * @param lockKey 锁的key
     * @param requestId 请求ID(用于释放锁时校验)
     * @param expireTime 过期时间(秒)
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
            log.error("Redis tryLock error, lockKey={}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
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
            log.error("Redis releaseLock error, lockKey={}", lockKey, e);
            return false;
        }
    }

    // ========== 限流(滑动窗口算法) ==========

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

    /**
     * Pipeline批量设置
     */
//    public void batchSet(Map<String, Object> keyValues, long timeout, TimeUnit unit) {
//        try {
//            long seconds = unit.toSeconds(timeout);
//            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//                keyValues.forEach((key, value) -> connection.setEx(key.getBytes(), seconds,
//                        Objects.requireNonNull(redisTemplate.getValueSerializer().serialize(value))));
//                return null;
//            });
//        } catch (Exception e) {
//            log.error("Redis batchSet error", e);
//        }
//    }
}
