package com.cjx.common.core.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 *
 * 与Hutool区别：
 * - Hutool的NetUtil主要处理网络相关操作
 * - 本类专注于从Request获取真实IP（考虑代理、负载均衡）
 *
 * @author system
 */
public class IpUtil {
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final int IP_MAX_LENGTH = 15;

    /**
     * 获取客户端真实IP地址
     * 支持多级反向代理
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getRemoteAddr();

        // 处理本地IP
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IP;
        }

        return ip;
    }

    /**
     * 判断IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 判断是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (StrUtil.isBlank(ip) || UNKNOWN.equals(ip)) {
            return false;
        }

        if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }

        // 192.168.x.x
        if (ip.startsWith("192.168.")) {
            return true;
        }

        // 10.x.x.x
        if (ip.startsWith("10.")) {
            return true;
        }

        // 172.16.x.x ~ 172.31.x.x
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * 获取IP归属地（需要集成第三方IP库，如ip2region）
     */
    public static String getIpLocation(String ip) {
        return "未知";
    }
}
