package com.cjx.common.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 聊天响应封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String content;
    private String conversationId;
    private String model;
    private TokenUsage usage;

    @Builder.Default
    private boolean success = true;

    private String errorMessage;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static AiChatResponse success(String content) {
        return AiChatResponse.builder().content(content).build();
    }

    public static AiChatResponse error(String msg) {
        return AiChatResponse.builder().success(false).errorMessage(msg).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
