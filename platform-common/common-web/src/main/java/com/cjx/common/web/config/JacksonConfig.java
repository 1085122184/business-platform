package com.cjx.common.web.config;

import cn.hutool.json.JSONNull;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cjx
 */
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            SimpleModule jsonNullModule = new SimpleModule();
            // 注册你原来的 JSONNull 序列化器
            jsonNullModule.addSerializer(JSONNull.class, new JSONNullSerializer());
            // 【关键修改点 1】同时注册你的模块 AND JavaTimeModule
            builder.modules(jsonNullModule, new JavaTimeModule());

            // 【关键修改点 2】强烈建议加上这一行
            // 否则 LocalDateTime 可能会变成 [2023, 11, 27, 10, 30] 这种数组格式，而不是字符串
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
