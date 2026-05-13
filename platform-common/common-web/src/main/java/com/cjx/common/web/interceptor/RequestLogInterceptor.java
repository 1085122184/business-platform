package com.cjx.common.web.interceptor;

import cn.hutool.core.util.IdUtil;
import com.cjx.common.core.utils.IpUtil;
import com.cjx.common.core.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
/**
 * 请求日志拦截器
 * 记录每个请求的基本信息和耗时
 *
 * @author system
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 生成请求ID
        String requestId = IdUtil.fastSimpleUUID();
        ThreadLocalUtil.setRequestId(requestId);

        // 记录开始时间
        request.setAttribute(START_TIME, System.currentTimeMillis());

        // 获取客户端IP
        String ip = IpUtil.getClientIp(request);

        // 记录请求信息
        log.info("请求开始 >>> requestId={}, method={}, uri={}, ip={}",
                requestId, request.getMethod(), request.getRequestURI(), ip);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            // 计算耗时
            Long startTime = (Long) request.getAttribute(START_TIME);
            long duration = System.currentTimeMillis() - startTime;

            String requestId = ThreadLocalUtil.getRequestId();

            // 记录请求完成信息
            log.info("请求完成 <<< requestId={}, status={}, duration={}ms",
                    requestId, response.getStatus(), duration);

            // 慢请求告警(超过3秒)
            if (duration > 3000) {
                log.warn("慢请求告警 !!! requestId={}, uri={}, duration={}ms",
                        requestId, request.getRequestURI(), duration);
            }

        } finally {
            // 清理ThreadLocal
            ThreadLocalUtil.clear();
        }
    }
}
