package com.cjx.common.ai.config;

import com.cjx.common.ai.advisor.LoggingAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.JdkClientHttpConnector;

import java.net.http.HttpClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiCommonConfig {

    // 1. 造出记忆库
    @Bean
    public ChatMemory chatMemory(AiProperties aiProperties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(aiProperties.getChat().getMemoryWindowSize())
                .build();
    }

    // 2. 造出 ChatClient，并挂载你写的日志拦截器
    @Bean
    public ChatClient customChatClient(ChatClient.Builder builder,
                                       ChatMemory chatMemory,
                                       AiProperties aiProperties,
                                       LoggingAdvisor loggingAdvisor) {
        log.info("[AI-Common] 初始化 ChatClient 成功！");
        return builder
                .defaultSystem(aiProperties.getChat().getSystemPrompt())
                .defaultAdvisors(
                        loggingAdvisor,
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Bean
    public WebClientCustomizer forceHttp11WebClientCustomizer() {
        return builder -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1) // 强行锁定 HTTP 1.1
                    .build();
            builder.clientConnector(new JdkClientHttpConnector(httpClient));
        };
    }

    /**
     * 🌟 核心修复 2：强制同步 RestClient 降级为 HTTP/1.1
     */
    @Bean
    public RestClientCustomizer forceHttp11RestClientCustomizer() {
        return builder -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1) // 强行锁定 HTTP 1.1
                    .build();
            builder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
        };
    }
}