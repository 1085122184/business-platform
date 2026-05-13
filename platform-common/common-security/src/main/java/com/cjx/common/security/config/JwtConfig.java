package com.cjx.common.security.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

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
    private String secretKey;

    /**
     * Token有效期(毫秒)，默认7天
     */
    private long expiration = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Token刷新期(毫秒)，默认3天
     */
    private long refreshTime = 3 * 24 * 60 * 60 * 1000L;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(secretKey) || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET_KEY must be configured and at least 32 bytes for HS256");
        }
    }
}
