package com.cjx.common.security.filter;

import cn.hutool.core.util.StrUtil;
import com.cjx.common.core.utils.ThreadLocalUtil;
import com.cjx.common.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * @author system
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 获取Token
        String token = getTokenFromRequest(request);

        if (StrUtil.isNotBlank(token)) {
            // 验证Token
            if (JwtUtil.validateToken(token)) {
                // 解析Token，设置到ThreadLocal
                Long userId = JwtUtil.getUserIdFromToken(token);
                String username = JwtUtil.getUsernameFromToken(token);

                ThreadLocalUtil.setUserId(userId);
                ThreadLocalUtil.setUsername(username);

                log.debug("Token验证成功: userId={}, username={}", userId, username);
            } else {
                log.warn("Token验证失败: {}", token);
            }
        }

        filterChain.doFilter(request, response);
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
