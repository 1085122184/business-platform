package com.cjx.common.security.filter;

import cn.hutool.core.util.StrUtil;
import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.common.core.utils.ThreadLocalUtil;
import com.cjx.common.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Resolves user identity and permissions from JWT, then binds them to the request context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final CaffeineCacheService caffeineCacheService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);

            if (StrUtil.isNotBlank(token) && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                ThreadLocalUtil.setUserId(userId);
                ThreadLocalUtil.setUsername(username);

                Set<String> permissions = resolvePermissions(userId, token);
                if (permissions != null && !permissions.isEmpty()) {
                    ThreadLocalUtil.setPermissions(permissions);
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Token validated and security context populated: userId={}, username={}", userId, username);
            }

            filterChain.doFilter(request, response);
        } finally {
            ThreadLocalUtil.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private Set<String> resolvePermissions(Long userId, String token) {
        String cacheKey = "perms:" + userId;
        @SuppressWarnings("unchecked")
        Set<String> permissions = caffeineCacheService.get(CacheType.USER_PERMISSIONS, cacheKey, Set.class);
        if (permissions != null) {
            return permissions;
        }

        permissions = jwtUtil.getPermissionsFromToken(token);
        if (permissions != null && !permissions.isEmpty()) {
            log.debug("Loaded permissions from token claim as cache fallback, userId={}", userId);
            return permissions;
        }

        log.debug("No permissions found in cache or token claim, userId={}", userId);
        return null;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
