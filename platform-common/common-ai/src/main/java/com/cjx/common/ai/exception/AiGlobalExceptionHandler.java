package com.cjx.common.ai.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 模块全局异常处理器
 * <p>引入 ai-common 后自动生效（ComponentScan 覆盖），统一返回 JSON 格式错误信息。
 */
@Slf4j
@RestControllerAdvice
public class AiGlobalExceptionHandler {

    @ExceptionHandler(AiException.class)
    public ResponseEntity<Map<String, Object>> handleAiException(AiException e) {
        log.error("[AI] 异常: code={}, msg={}", e.getCode(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success",   false,
                        "code",      e.getCode(),
                        "message",   e.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException e) {
        log.warn("[AI] 参数异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success",   false,
                        "code",      400,
                        "message",   e.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
