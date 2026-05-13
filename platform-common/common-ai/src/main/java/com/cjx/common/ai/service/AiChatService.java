package com.cjx.common.ai.service;

import com.cjx.common.ai.config.AiProperties;
import com.cjx.common.ai.exception.AiException;
import com.cjx.common.ai.model.AiChatRequest;
import com.cjx.common.ai.model.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * AI 聊天服务（Spring AI 1.1.3）
 *
 * <p>封装普通聊天、流式聊天、结构化输出三种模式。
 * 业务模块直接 {@code @Autowired AiChatService} 使用，无需关心底层细节。
 *
 * <pre>
 * // 普通聊天
 * AiChatResponse resp = aiChatService.chat(AiChatRequest.of("你好"));
 *
 * // 带会话记忆
 * AiChatResponse resp = aiChatService.chat(AiChatRequest.of("我叫小明", "session-001"));
 *
 * // 流式（配合 SSE 接口）
 * Flux&lt;String&gt; flux = aiChatService.stream(AiChatRequest.of("讲个故事"));
 *
 * // 一行问答
 * String answer = aiChatService.ask("1+1=?");
 * </pre>
 * @author cuijixu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiChatService {

    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    // ── 普通同步聊天 ──────────────────────────────────────────────────────────

    public AiChatResponse chat(AiChatRequest request) {
        try {
            ChatResponse response = buildSpec(request).call().chatResponse();
            if (response == null) {
                return AiChatResponse.error("AI 返回为空");
            }

            // 1.1.x 中 AssistantMessage.getText() 替代了旧的 getContent()
            String content = response.getResult().getOutput().getText();
            ChatResponseMetadata metadata = response.getMetadata();

            AiChatResponse.TokenUsage usage = null;
            if (metadata != null && metadata.getUsage() != null) {
                Usage u = metadata.getUsage();
                usage = AiChatResponse.TokenUsage.builder()
                        .promptTokens(u.getPromptTokens()     != null ? u.getPromptTokens().intValue()     : null)
                        .completionTokens(u.getCompletionTokens() != null ? u.getCompletionTokens().intValue() : null)
                        .totalTokens(u.getTotalTokens()       != null ? u.getTotalTokens().intValue()       : null)
                        .build();
            }

            return AiChatResponse.builder()
                    .content(content)
                    .conversationId(request.getConversationId())
                    .model(metadata != null ? metadata.getModel() : null)
                    .usage(usage)
                    .build();

        } catch (Exception e) {
            log.error("[AiChatService] 聊天失败: {}", e.getMessage(), e);
            throw new AiException("聊天失败: " + e.getMessage(), e);
        }
    }

    // ── 流式聊天 ──────────────────────────────────────────────────────────────

    public Flux<String> stream(AiChatRequest request) {
        try {
            return buildSpec(request)
                    .stream()
                    .content()
                    .doOnError(e -> log.error("[AiChatService] 流式异常: {}", e.getMessage(), e));
        } catch (Exception e) {
            log.error("[AiChatService] 流式构建失败: {}", e.getMessage(), e);
            return Flux.error(new AiException("流式失败: " + e.getMessage(), e));
        }
    }

    // ── 结构化输出 ─────────────────────────────────────────────────────────────

    /**
     * AI 返回内容直接映射为指定 Java 类型（利用 Spring AI 内置 OutputConverter）
     *
     * <pre>
     * record BookInfo(String title, String author) {}
     * BookInfo book = aiChatService.chatAs("介绍《三体》", BookInfo.class);
     * </pre>
     */
    public <T> T chatAs(String message, Class<T> responseType) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .call()
                    .entity(responseType);
        } catch (Exception e) {
            log.error("[AiChatService] 结构化输出失败: {}", e.getMessage(), e);
            throw new AiException("结构化输出失败: " + e.getMessage(), e);
        }
    }

    // ── 快捷方法 ───────────────────────────────────────────────────────────────

    /** 一行调用，直接返回字符串 */
    public String ask(String message) {
        return chat(AiChatRequest.of(message)).getContent();
    }

    // ── 私有方法 ───────────────────────────────────────────────────────────────

    private ChatClient.ChatClientRequestSpec buildSpec(AiChatRequest request) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                .user(request.getMessage());

        if (StringUtils.hasText(request.getSystemPrompt())) {
            spec = spec.system(request.getSystemPrompt());
        }

        // 1.1.x 会话 ID 参数键：ChatMemory.CONVERSATION_ID
        if (StringUtils.hasText(request.getConversationId())
                && aiProperties.getChat().isMemoryEnabled()) {
            final String cid = request.getConversationId();
            spec = spec.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, cid));
        }

        return spec;
    }
}
