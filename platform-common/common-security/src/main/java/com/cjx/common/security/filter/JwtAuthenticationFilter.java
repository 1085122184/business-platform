package com.cjx.common.security.filter;

import cn.hutool.core.util.StrUtil;
import com.cjx.common.core.utils.ThreadLocalUtil;
import com.cjx.common.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 解析请求中的JWT Token，验证后设置用户信息到ThreadLocal
 *
 * @author system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 获取Token
            String token = getTokenFromRequest(request);

            if (StrUtil.isNotBlank(token)) {
                // 验证Token
                if (jwtUtil.validateToken(token)) {
                    // 解析Token，设置到ThreadLocal
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);

                    ThreadLocalUtil.setUserId(userId);
                    ThreadLocalUtil.setUsername(username);

                    log.debug("Token验证成功: userId={}, username={}", userId, username);
                } else {
                    // Token验证失败，日志中脱敏显示
                    log.warn("Token验证失败: {}", token.length() > 10 ? token.substring(0, 10) + "..." : "***");
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // 清理ThreadLocal，防止内存泄漏
            ThreadLocalUtil.clear();
        }
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
