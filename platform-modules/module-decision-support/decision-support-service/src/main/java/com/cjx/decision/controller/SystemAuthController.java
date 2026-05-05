package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.auth.AuthProfileResponse;
import com.cjx.decision.dto.auth.DingTalkBridgeResponse;
import com.cjx.decision.dto.auth.DingTalkLoginRequest;
import com.cjx.decision.dto.auth.LoginRequest;
import com.cjx.decision.dto.auth.LoginResponse;
import com.cjx.decision.dto.auth.LoginTicketRequest;
import com.cjx.decision.service.SystemAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints for login and current-user profile.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "登录认证", description = "登录、退出和当前用户信息接口")
public class SystemAuthController {

    private final SystemAuthService systemAuthService;

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return Result.success("登录成功", systemAuthService.login(request, httpServletRequest));
    }

    @Operation(summary = "移动端钉钉免密登录")
    @PostMapping("/dingtalk/login")
    public Result<LoginResponse> loginByDingTalk(@Valid @RequestBody DingTalkLoginRequest request,
                                                 HttpServletRequest httpServletRequest) {
        return Result.success("登录成功", systemAuthService.loginByDingTalk(request, httpServletRequest));
    }

    @Operation(summary = "PC 端钉钉中转登录")
    @PostMapping("/dingtalk/bridge-login")
    public Result<DingTalkBridgeResponse> createPcBridgeLogin(@Valid @RequestBody DingTalkLoginRequest request,
                                                              @RequestParam(value = "redirect", required = false) String redirect,
                                                              HttpServletRequest httpServletRequest) {
        return Result.success(systemAuthService.createPcBridgeLogin(request, redirect, httpServletRequest));
    }

    @Operation(summary = "消费一次性登录票据")
    @PostMapping("/login-ticket/consume")
    public Result<LoginResponse> consumeLoginTicket(@Valid @RequestBody LoginTicketRequest request,
                                                    HttpServletRequest httpServletRequest) {
        return Result.success("登录成功", systemAuthService.consumeLoginTicket(request, httpServletRequest));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest httpServletRequest) {
        return Result.success("退出成功", systemAuthService.logout(httpServletRequest));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/profile")
    public Result<AuthProfileResponse> getProfile() {
        return Result.success(systemAuthService.getProfile());
    }
}
