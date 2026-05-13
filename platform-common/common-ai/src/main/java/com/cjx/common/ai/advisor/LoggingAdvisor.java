package com.cjx.common.ai.advisor;

import com.cjx.common.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 请求/响应日志 Advisor（Spring AI 1.1.3）
 *
 * <p>Spring AI 1.1.x Advisor 接口全部重命名：
 * <pre>
 *   旧（1.0.x）                   新（1.1.x）
 *   CallAroundAdvisor        →    CallAdvisor
 *   StreamAroundAdvisor      →    StreamAdvisor
 *   CallAroundAdvisorChain   →    CallAdvisorChain
 *   StreamAroundAdvisorChain →    StreamAdvisorChain
 *   AdvisedRequest           →    ChatClientRequest
 *   AdvisedResponse          →    ChatClientResponse
 *   aroundCall()             →    adviseCall()
 *   aroundStream()           →    adviseStream()
 * </pre>
 *
 * <p>通过 {@code ai.common.log.log-request/log-response} 开关控制日志内容。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingAdvisor implements CallAdvisor, StreamAdvisor {

    private final AiProperties aiProperties;

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // 最先执行
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logRequest(request);
        long start = System.currentTimeMillis();
        ChatClientResponse response = chain.nextCall(request);
        logResponse(response, System.currentTimeMillis() - start);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        logRequest(request);
        long start = System.currentTimeMillis();
        return chain.nextStream(request)
                .doOnComplete(() ->
                        log.info("[AI] 流式完成，耗时={}ms", System.currentTimeMillis() - start));
    }

    private void logRequest(ChatClientRequest request) {
        if (aiProperties.getLog().isLogRequest()) {
            log.info("[AI][Request] {}", request.prompt());
        } else {
            log.debug("[AI][Request] 收到请求");
        }
    }

    private void logResponse(ChatClientResponse response, long ms) {
        if (aiProperties.getLog().isLogResponse()) {
            String text = "";
            try {
                text = response.chatResponse().getResult().getOutput().getText();
            } catch (Exception ignored) {}
            log.info("[AI][Response] 耗时={}ms, content={}", ms, text);
        } else {
            log.debug("[AI][Response] 完成，耗时={}ms", ms);
        }
    }
}
