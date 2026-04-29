package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.enums.ResultCode;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.common.core.utils.ThreadLocalUtil;
import com.cjx.common.security.config.JwtConfig;
import com.cjx.common.security.utils.JwtUtil;
import com.cjx.decision.dto.auth.AuthProfileResponse;
import com.cjx.decision.dto.auth.LoginRequest;
import com.cjx.decision.dto.auth.LoginResponse;
import com.cjx.decision.dto.auth.LoginUserInfoResponse;
import com.cjx.decision.entity.frorcl.system.SysLoginLog;
import com.cjx.decision.entity.frorcl.system.SysUser;
import com.cjx.decision.repository.frorcl.system.SysLoginLogRepository;
import com.cjx.decision.repository.frorcl.system.SysMenuRepository;
import com.cjx.decision.repository.frorcl.system.SysRoleRepository;
import com.cjx.decision.repository.frorcl.system.SysUserRepository;
import com.cjx.decision.service.SystemAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Authentication implementation backed by SYS_USER and RBAC tables.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "frorclTransactionManager")
public class SystemAuthServiceImpl implements SystemAuthService {

    private static final String NORMAL_DEL_FLAG = "0";

    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysLoginLogRepository sysLoginLogRepository;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final CaffeineCacheService caffeineCacheService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        String username = request.getUsername().trim();
        String loginIp = resolveClientIp(httpServletRequest);
        String userAgent = resolveUserAgent(httpServletRequest);

        SysUser user = sysUserRepository.findByUsernameAndDelFlag(username, NORMAL_DEL_FLAG)
                .orElseThrow(() -> unauthorized(username, null, loginIp, userAgent, "用户名或密码错误"));

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            writeLoginLog(user.getId(), username, loginIp, userAgent, 0, "用户已禁用");
            throw new BusinessException(String.valueOf(ResultCode.USER_DISABLED.getCode()), "用户已禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw unauthorized(username, user.getId(), loginIp, userAgent, "用户名或密码错误");
        }

        List<String> roles = sysRoleRepository.findRoleKeysByUserId(user.getId());
        List<String> permissions = sysMenuRepository.findPermissionsByUserId(user.getId());
        Set<String> permissionSet = new LinkedHashSet<>(permissions);

        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), permissionSet);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        caffeineCacheService.put(CacheType.USER_PERMISSIONS, permissionCacheKey(user.getId()), permissionSet);

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        sysUserRepository.save(user);
        writeLoginLog(user.getId(), username, loginIp, userAgent, 1, "登录成功");

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtConfig.getExpiration() / 1000);
        response.setUserInfo(toUserInfo(user));
        response.setRoles(roles);
        response.setPermissions(List.copyOf(permissionSet));
        return response;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public boolean logout(HttpServletRequest httpServletRequest) {
        Long userId = ThreadLocalUtil.getUserId();
        if (userId != null) {
            caffeineCacheService.remove(CacheType.USER_PERMISSIONS, permissionCacheKey(userId));
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public AuthProfileResponse getProfile() {
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new BusinessException(String.valueOf(ResultCode.UNAUTHORIZED.getCode()), "未登录或登录已失效");
        }

        SysUser user = sysUserRepository.findByIdAndDelFlag(userId, NORMAL_DEL_FLAG)
                .orElseThrow(() -> new BusinessException(String.valueOf(ResultCode.UNAUTHORIZED.getCode()), "用户不存在或已删除"));

        List<String> roles = sysRoleRepository.findRoleKeysByUserId(userId);
        List<String> permissions = sysMenuRepository.findPermissionsByUserId(userId);

        AuthProfileResponse response = new AuthProfileResponse();
        response.setUserInfo(toUserInfo(user));
        response.setRoles(roles);
        response.setPermissions(permissions);
        return response;
    }

    private BusinessException unauthorized(String username,
                                           Long userId,
                                           String loginIp,
                                           String userAgent,
                                           String message) {
        writeLoginLog(userId, username, loginIp, userAgent, 0, message);
        return new BusinessException(String.valueOf(ResultCode.UNAUTHORIZED.getCode()), message);
    }

    private LoginUserInfoResponse toUserInfo(SysUser user) {
        LoginUserInfoResponse userInfo = new LoginUserInfoResponse();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setStatus(user.getStatus());
        return userInfo;
    }

    private void writeLoginLog(Long userId,
                               String username,
                               String loginIp,
                               String userAgent,
                               int loginStatus,
                               String loginMessage) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginStatus(loginStatus);
            loginLog.setLoginMessage(loginMessage);
            loginLog.setLoginIp(loginIp);
            loginLog.setUserAgent(userAgent);
            sysLoginLogRepository.save(loginLog);
        } catch (Exception e) {
            log.warn("Failed to persist login log for username={}", username, e);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
    }

    private String resolveUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.hasText(userAgent) ? userAgent : null;
    }

    private String permissionCacheKey(Long userId) {
        return "perms:" + userId;
    }
}
