package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.enums.ResultCode;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.common.core.utils.ThreadLocalUtil;
import com.cjx.common.dingtalk.dto.DingTalkUserInfo;
import com.cjx.common.dingtalk.utils.DingTalkUtil;
import com.cjx.common.security.config.JwtConfig;
import com.cjx.common.security.utils.JwtUtil;
import com.cjx.decision.dto.auth.AuthProfileResponse;
import com.cjx.decision.dto.auth.DingTalkBridgeResponse;
import com.cjx.decision.dto.auth.DingTalkLoginRequest;
import com.cjx.decision.dto.auth.LoginRequest;
import com.cjx.decision.dto.auth.LoginResponse;
import com.cjx.decision.dto.auth.LoginTicketRequest;
import com.cjx.decision.dto.auth.LoginTicketResponse;
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
    private static final String LOGIN_TICKET_CACHE_PREFIX = "login-ticket:";
    private static final int LOGIN_TICKET_EXPIRES_IN_SECONDS = 120;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysLoginLogRepository sysLoginLogRepository;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final CaffeineCacheService caffeineCacheService;
    private final DingTalkUtil dingTalkUtil;

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

        writeLoginLog(user.getId(), username, loginIp, userAgent, 1, "登录成功");
        return buildLoginResponse(user, loginIp);
    }

    @Override
    public LoginResponse loginByDingTalk(DingTalkLoginRequest request, HttpServletRequest httpServletRequest) {
        String loginIp = resolveClientIp(httpServletRequest);
        String userAgent = resolveUserAgent(httpServletRequest);

        try {
            DingTalkUserInfo codeUserInfo = dingTalkUtil.getUserInfoByAuthCode(request.getAuthCode());
            String dingUserId = codeUserInfo.getUserId();
            if (!StringUtils.hasText(dingUserId)) {
                throw unauthorized("dingtalk", null, loginIp, userAgent, "钉钉用户ID为空");
            }

            DingTalkUserInfo detail = dingTalkUtil.getUserDetail(dingUserId);
            SysUser user = resolveDingTalkUser(dingUserId, detail, loginIp, userAgent);
            if (!Integer.valueOf(1).equals(user.getStatus())) {
                writeLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 0, "用户已禁用");
                throw new BusinessException(String.valueOf(ResultCode.USER_DISABLED.getCode()), "用户已禁用");
            }

            if (!dingUserId.equals(user.getDingUserId())) {
                user.setDingUserId(dingUserId);
            }
            fillUserProfileFromDingTalk(user, detail);
            fillDingTalkIdentity(user, codeUserInfo, detail);
            writeLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 1, "钉钉免密登录成功");
            return buildLoginResponse(user, loginIp);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("DingTalk login failed", e);
            throw new BusinessException(String.valueOf(ResultCode.UNAUTHORIZED.getCode()), "钉钉免密登录失败");
        }
    }

    @Override
    public DingTalkBridgeResponse createPcBridgeLogin(DingTalkLoginRequest request, String redirect, HttpServletRequest httpServletRequest) {
        String loginIp = resolveClientIp(httpServletRequest);
        String userAgent = resolveUserAgent(httpServletRequest);

        try {
            DingTalkUserInfo codeUserInfo = dingTalkUtil.getUserInfoByAuthCode(request.getAuthCode());
            String dingUserId = codeUserInfo.getUserId();
            if (!StringUtils.hasText(dingUserId)) {
                throw unauthorized("dingtalk-bridge", null, loginIp, userAgent, "钉钉用户ID为空");
            }

            DingTalkUserInfo detail = dingTalkUtil.getUserDetail(dingUserId);
            SysUser user = resolveDingTalkUser(dingUserId, detail, loginIp, userAgent);
            if (!Integer.valueOf(1).equals(user.getStatus())) {
                writeLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 0, "用户已禁用");
                throw new BusinessException(String.valueOf(ResultCode.USER_DISABLED.getCode()), "用户已禁用");
            }

            if (!dingUserId.equals(user.getDingUserId())) {
                user.setDingUserId(dingUserId);
            }
            fillUserProfileFromDingTalk(user, detail);
            fillDingTalkIdentity(user, codeUserInfo, detail);
            writeLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 1, "钉钉 PC 中转登录成功");

            LoginTicketResponse ticketResponse = createLoginTicket(user, loginIp);
            String targetRedirect = StringUtils.hasText(redirect) ? redirect.trim() : "/";
            String externalUrl = "/login?loginTicket=" + ticketResponse.getTicket()
                    + "&redirect=" + java.net.URLEncoder.encode(targetRedirect, java.nio.charset.StandardCharsets.UTF_8);

            DingTalkBridgeResponse response = new DingTalkBridgeResponse();
            response.setExternalUrl(externalUrl);
            response.setExpiresIn(ticketResponse.getExpiresIn());
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("DingTalk PC bridge login failed", e);
            throw new BusinessException(String.valueOf(ResultCode.UNAUTHORIZED.getCode()), "钉钉 PC 中转登录失败");
        }
    }

    @Override
    public LoginResponse consumeLoginTicket(LoginTicketRequest request, HttpServletRequest httpServletRequest) {
        String ticket = request.getTicket().trim();
        String cacheKey = loginTicketCacheKey(ticket);
        LoginTicketPayload payload = caffeineCacheService.get(CacheType.TOKEN, cacheKey, LoginTicketPayload.class);
        caffeineCacheService.remove(CacheType.TOKEN, cacheKey);
        String loginIp = resolveClientIp(httpServletRequest);
        String userAgent = resolveUserAgent(httpServletRequest);

        if (payload == null || payload.expiresAt().isBefore(LocalDateTime.now())) {
            throw unauthorized("login-ticket", null, loginIp, userAgent, "login ticket expired or invalid");
        }

        SysUser user = sysUserRepository.findByIdAndDelFlag(payload.userId(), NORMAL_DEL_FLAG)
                .orElseThrow(() -> unauthorized("login-ticket", payload.userId(), loginIp, userAgent, "user does not exist"));
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            writeLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 0, "user disabled");
            throw new BusinessException(String.valueOf(ResultCode.USER_DISABLED.getCode()), "user disabled");
        }

        writeLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 1, "one-time login ticket consumed");
        return buildLoginResponse(user, loginIp);
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

    private LoginResponse buildLoginResponse(SysUser user, String loginIp) {
        List<String> roles = sysRoleRepository.findRoleKeysByUserId(user.getId());
        List<String> permissions = sysMenuRepository.findPermissionsByUserId(user.getId());
        Set<String> permissionSet = new LinkedHashSet<>(permissions);

        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), permissionSet);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        caffeineCacheService.put(CacheType.USER_PERMISSIONS, permissionCacheKey(user.getId()), permissionSet);

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        sysUserRepository.save(user);

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

    private LoginTicketResponse createLoginTicket(SysUser user, String loginIp) {
        String ticket = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(LOGIN_TICKET_EXPIRES_IN_SECONDS);
        caffeineCacheService.put(CacheType.TOKEN, loginTicketCacheKey(ticket), new LoginTicketPayload(user.getId(), expiresAt));

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        sysUserRepository.save(user);

        LoginTicketResponse response = new LoginTicketResponse();
        response.setTicket(ticket);
        response.setExpiresIn(LOGIN_TICKET_EXPIRES_IN_SECONDS);
        return response;
    }

    private SysUser resolveDingTalkUser(String dingUserId,
                                        DingTalkUserInfo detail,
                                        String loginIp,
                                        String userAgent) {
        return sysUserRepository.findByDingUserIdAndDelFlag(dingUserId, NORMAL_DEL_FLAG)
                .or(() -> findAndBindByMobile(dingUserId, detail))
                .orElseThrow(() -> {
                    String username = detail != null && StringUtils.hasText(detail.getMobile())
                            ? detail.getMobile()
                            : dingUserId;
                    return unauthorized(username, null, loginIp, userAgent, "钉钉账号未绑定系统用户");
                });
    }

    private java.util.Optional<SysUser> findAndBindByMobile(String dingUserId, DingTalkUserInfo detail) {
        if (detail == null || !StringUtils.hasText(detail.getMobile())) {
            return java.util.Optional.empty();
        }
        return sysUserRepository.findByMobileAndDelFlag(detail.getMobile(), NORMAL_DEL_FLAG)
                .map(user -> {
                    user.setDingUserId(dingUserId);
                    fillUserProfileFromDingTalk(user, detail);
                    fillDingTalkIdentity(user, null, detail);
                    return user;
                });
    }

    private void fillDingTalkIdentity(SysUser user, DingTalkUserInfo codeUserInfo, DingTalkUserInfo detail) {
        String unionId = null;
        if (detail != null && StringUtils.hasText(detail.getUnionId())) {
            unionId = detail.getUnionId();
        } else if (codeUserInfo != null && StringUtils.hasText(codeUserInfo.getUnionId())) {
            unionId = codeUserInfo.getUnionId();
        }
        if (StringUtils.hasText(unionId)) {
            user.setDingUnionId(unionId);
        }
    }

    private void fillUserProfileFromDingTalk(SysUser user, DingTalkUserInfo detail) {
        if (detail == null) {
            return;
        }
        if (StringUtils.hasText(detail.getName()) && !StringUtils.hasText(user.getRealName())) {
            user.setRealName(detail.getName());
        }
        if (StringUtils.hasText(detail.getName()) && !StringUtils.hasText(user.getNickname())) {
            user.setNickname(detail.getName());
        }
        if (StringUtils.hasText(detail.getMobile()) && !StringUtils.hasText(user.getMobile())) {
            user.setMobile(detail.getMobile());
        }
        if (StringUtils.hasText(detail.getEmail()) && !StringUtils.hasText(user.getEmail())) {
            user.setEmail(detail.getEmail());
        }
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

    private String loginTicketCacheKey(String ticket) {
        return LOGIN_TICKET_CACHE_PREFIX + ticket;
    }

    private record LoginTicketPayload(Long userId, LocalDateTime expiresAt) {
    }
}
