package com.cjx.common.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 聊天请求封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    /** 用户消息 */
    private String message;

    /** 会话 ID（传 null 则不使用记忆） */
    private String conversationId;

    /** 自定义系统提示词（覆盖默认值） */
    private String systemPrompt;

    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

    public static AiChatRequest of(String message) {
        return AiChatRequest.builder().message(message).build();
    }

    public static AiChatRequest of(String message, String conversationId) {
        return AiChatRequest.builder().message(message).conversationId(conversationId).build();
    }
}
