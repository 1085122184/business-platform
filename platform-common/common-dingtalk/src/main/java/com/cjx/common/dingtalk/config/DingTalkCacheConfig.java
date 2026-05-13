package com.cjx.common.dingtalk.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 钉钉缓存配置常量
 */
public class DingTalkCacheConfig {

    /**
     * AccessToken缓存键
     */
    public static final String ACCESS_TOKEN_KEY = "ding-talk:access_token";

    /**
     * JsApiTicket缓存键
     */
    public static final String JSAPI_TICKET_KEY = "ding-talk:jsapi_ticket";

    /**
     * 用户信息缓存键前缀
     */
    public static final String USER_INFO_KEY_PREFIX = "ding-talk:user:";

    /**
     * 部门信息缓存键前缀
     */
    public static final String DEPT_INFO_KEY_PREFIX = "ding-talk:dept:";

    /**
     * AccessToken缓存时间（110分钟，钉钉token有效期2小时）
     */
    public static final long ACCESS_TOKEN_TTL = 110 * 60 * 1000L;

    /**
     * JsApiTicket缓存时间（110分钟）
     */
    public static final long JSAPI_TICKET_TTL = 110 * 60 * 1000L;

    /**
     * 用户信息缓存时间（30分钟）
     */
    public static final long USER_INFO_TTL = 30 * 60 * 1000L;

    /**
     * 部门信息缓存时间（1小时）
     */
    public static final long DEPT_INFO_TTL = 60 * 60 * 1000L;

    /**
     * 构建用户信息缓存键
     */
    public static String buildUserInfoKey(String userId) {
        return USER_INFO_KEY_PREFIX + userId;
    }

    /**
     * 构建部门信息缓存键
     */
    public static String buildDeptInfoKey(Long deptId) {
        return DEPT_INFO_KEY_PREFIX + deptId;
    }

}
