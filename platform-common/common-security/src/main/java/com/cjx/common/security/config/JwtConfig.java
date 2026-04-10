package com.cjx.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 * 从配置文件读取JWT相关配置，避免硬编码
 *
 * @author system
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥(生产环境请使用足够长的密钥)
     */
    private String secretKey = "your-256-bit-secret-key-here-must-be-long-enough-for-hs256";

    /**
     * Token有效期(毫秒)，默认7天
     */
    private long expiration = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Token刷新期(毫秒)，默认3天
     */
    private long refreshTime = 3 * 24 * 60 * 60 * 1000L;
}
