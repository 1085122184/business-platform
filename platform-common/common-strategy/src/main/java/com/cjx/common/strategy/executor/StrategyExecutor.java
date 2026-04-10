package com.cjx.common.strategy.executor;

import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.common.strategy.metadata.StrategyMetadata;
import com.cjx.common.strategy.registry.StrategyRegistry;
import com.cjx.common.strategy.result.StrategyResult;
import com.cjx.common.strategy.validator.StrategyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 策略执行器
 * 负责策略的调用、监控、异常处理
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyExecutor {

    private final StrategyRegistry strategyRegistry;
    private final StrategyValidator strategyValidator;

    /**
     * 执行策略（主入口）
     *
     * @param strategyKey 策略Key
     * @param context 执行上下文
     * @return 执行结果
     */
    public StrategyResult<?> execute(String strategyKey, StrategyContext context) {

        // 参数校验
        validateParams(strategyKey, context);

        // 设置TraceId
        ensureTraceId(context);

        long startTime = System.currentTimeMillis();
        StrategyResult<?> result;

        try {
            log.info("[策略执行开始] strategyKey={}, traceId={}", strategyKey, context.getTraceId());

            // 获取策略元数据
            StrategyMetadata metadata = strategyRegistry.getMetadata(strategyKey);

            // 执行策略
            result = doExecute(metadata, context);

            // 设置执行时长和TraceId
            enrichResult(result, context, startTime);

            log.info("[策略执行成功] strategyKey={}, traceId={}, duration={}ms, success={}",
                    strategyKey, context.getTraceId(), result.getDuration(), result.getSuccess());

        } catch (BusinessException e) {
            log.warn("[策略执行业务异常] strategyKey={}, traceId={}, errorCode={}, message={}",
                    strategyKey, context.getTraceId(), e.getErrorCode(), e.getMessage());
            result = StrategyResult.businessError(e.getErrorCode(), e.getMessage());
            enrichResult(result, context, startTime);

        } catch (Exception e) {
            log.error("[策略执行系统异常] strategyKey={}, traceId={}",
                    strategyKey, context.getTraceId(), e);
            result = StrategyResult.systemError("系统异常: " + e.getMessage());
            enrichResult(result, context, startTime);
        }

        return result;
    }

    /**
     * 异步执行策略
     *
     * @param strategyKey 策略Key
     * @param context 执行上下文
     * @return CompletableFuture
     */
    public CompletableFuture<StrategyResult<?>> executeAsync(String strategyKey, StrategyContext context) {
        return CompletableFuture.supplyAsync(() -> execute(strategyKey, context));
    }

    /**
     * 执行策略的核心逻辑
     */
    private StrategyResult<?> doExecute(StrategyMetadata metadata, StrategyContext context) throws Exception {
        // 获取Bean实例
        Object bean = strategyRegistry.getBean(metadata.getBeanName());
        if (bean == null) {
            throw new BusinessException("BEAN_NOT_FOUND",
                    "Bean实例不存在: " + metadata.getBeanName());
        }
        // 获取目标方法
        Method method = metadata.getTargetMethod();
        // 调用方法
        try {
            Object result = method.invoke(bean, context);
            // 处理返回值
            if (result instanceof StrategyResult) {
                return (StrategyResult<?>) result;
            } else {
                return StrategyResult.success(result);
            }
        } catch (InvocationTargetException e) {
            Throwable realError = e.getCause();
            log.error("策略执行异常: errorType={}, message={}", realError.getClass().getName(), realError.getMessage());
            return StrategyResult.failure(realError.getClass().getName(), realError.getMessage());
        }
    }

    /**
     * 参数校验
     */
    private void validateParams(String strategyKey, StrategyContext context) {
        if (!StringUtils.hasText(strategyKey)) {
            throw new BusinessException("STRATEGY_KEY_EMPTY", "策略Key不能为空");
        }

        if (context == null) {
            throw new BusinessException("CONTEXT_NULL", "执行上下文不能为空");
        }
    }

    /**
     * 确保TraceId存在
     */
    private void ensureTraceId(StrategyContext context) {
        if (!StringUtils.hasText(context.getTraceId())) {
            context.setTraceId(generateTraceId());
        }
    }

    /**
     * 丰富结果信息
     */
    private void enrichResult(StrategyResult<?> result, StrategyContext context, long startTime) {
        if (result != null) {
            result.setDuration(System.currentTimeMillis() - startTime);
            result.setTraceId(context.getTraceId());
        }
    }

    /**
     * 生成TraceId
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
