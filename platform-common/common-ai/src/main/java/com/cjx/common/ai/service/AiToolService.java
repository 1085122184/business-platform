package com.cjx.common.ai.service;

import com.cjx.common.ai.exception.AiException;
import com.cjx.common.ai.tools.BuiltinTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 工具调用服务（Function Calling）（Spring AI 1.1.3）
 *
 * <pre>
 * // 使用内置工具（时间、计算、字数）
 * String result = aiToolService.chatWithBuiltinTools("现在几点了？");
 *
 * // 注入自定义工具（方法加 @Tool 注解即可）
 * String result = aiToolService.chatWithTools("查询订单 #123", myOrderTool);
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiToolService {

    private final ChatClient   chatClient;
    private final BuiltinTools builtinTools;

    public String chatWithBuiltinTools(String message) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .tools(builtinTools)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[AiToolService] 工具调用失败: {}", e.getMessage(), e);
            throw new AiException("工具调用失败: " + e.getMessage(), e);
        }
    }

    public Flux<String> streamWithBuiltinTools(String message) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .tools(builtinTools)
                    .stream()
                    .content();
        } catch (Exception e) {
            return Flux.error(new AiException("流式工具调用失败: " + e.getMessage(), e));
        }
    }

    /** 传入任意自定义工具对象（方法需用 @Tool 注解标注） */
    public String chatWithTools(String message, Object... tools) {
        try {
            return chatClient.prompt()
                    .user(message)
                    .tools(tools)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[AiToolService] 自定义工具调用失败: {}", e.getMessage(), e);
            throw new AiException("自定义工具调用失败: " + e.getMessage(), e);
        }
    }
}
