package com.cjx.common.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal工具类
 * 用于存储当前线程的用户信息、请求ID等
 *
 * @author system
 */
public class ThreadLocalUtil {
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    private static final String PERMISSIONS_KEY = "permissions";

    /**
     * 设置值
     */
    public static void set(String key, Object value) {
        Map<String, Object> map = getMap();
        map.put(key, value);
    }

    /**
     * 获取值
     */
    public static Object get(String key) {
        return getMap().get(key);
    }

    /**
     * 获取值并转换类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 移除值
     */
    public static void remove(String key) {
        getMap().remove(key);
    }

    /**
     * 清空当前线程的所有值
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }

    /**
     * 获取Map
     */
    private static Map<String, Object> getMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 设置当前用户权限列表
     */
    public static void setPermissions(java.util.Set<String> permissions) {
        set(PERMISSIONS_KEY, permissions);
    }

    /**
     * 获取当前用户权限列表
     */
    @SuppressWarnings("unchecked")
    public static java.util.Set<String> getPermissions() {
        return get(PERMISSIONS_KEY, java.util.Set.class);
    }

    // ========== 常用方法封装 ==========

    /** 用户ID的Key */
    private static final String USER_ID_KEY = "userId";

    /** 用户名的Key */
    private static final String USERNAME_KEY = "username";

    /** 请求ID的Key */
    private static final String REQUEST_ID_KEY = "requestId";

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        set(USER_ID_KEY, userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return get(USER_ID_KEY, Long.class);
    }

    /**
     * 设置当前用户名
     */
    public static void setUsername(String username) {
        set(USERNAME_KEY, username);
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return get(USERNAME_KEY, String.class);
    }

    /**
     * 设置请求ID（用于日志追踪）
     */
    public static void setRequestId(String requestId) {
        set(REQUEST_ID_KEY, requestId);
    }

    /**
     * 获取请求ID
     */
    public static String getRequestId() {
        return get(REQUEST_ID_KEY, String.class);
    }
}
