package com.cjx.common.web.aspect;

import com.cjx.common.core.enums.ResultCode;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.IpUtil;
import com.cjx.common.core.utils.ThreadLocalUtil;
import com.cjx.common.redis.utils.RedisUtil;
import com.cjx.common.web.annotation.RateLimit;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 限流切面
 * @author system
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(com.cjx.common.web.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);

        // 构建限流key
        String key = buildKey(rateLimit);

        // 检查限流
        boolean allowed = redisUtil.checkRateLimit(key, rateLimit.limit(), rateLimit.window());

        if (!allowed) {
            log.warn("触发限流: key={}, limit={}, window={}", key, rateLimit.limit(), rateLimit.window());
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS.getMessage());
        }

        // 继续执行
        return joinPoint.proceed();
    }

    /**
     * 构建限流key
     */
    private String buildKey(RateLimit rateLimit) {
        StringBuilder key = new StringBuilder("rate:limit:");
        key.append(rateLimit.key()).append(":");

        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            // 根据IP限流
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                key.append(IpUtil.getClientIp(request));
            }
        } else {
            // 根据用户ID限流
            Long userId = ThreadLocalUtil.getUserId();
            key.append(userId != null ? userId : "anonymous");
        }

        return key.toString();
    }
}
