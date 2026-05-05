package com.cjx.decision.service;

import com.cjx.decision.dto.auth.AuthProfileResponse;
import com.cjx.decision.dto.auth.DingTalkBridgeResponse;
import com.cjx.decision.dto.auth.DingTalkLoginRequest;
import com.cjx.decision.dto.auth.LoginRequest;
import com.cjx.decision.dto.auth.LoginResponse;
import com.cjx.decision.dto.auth.LoginTicketRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Authentication service.
 */
public interface SystemAuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest);

    LoginResponse loginByDingTalk(DingTalkLoginRequest request, HttpServletRequest httpServletRequest);

    DingTalkBridgeResponse createPcBridgeLogin(DingTalkLoginRequest request, String redirect, HttpServletRequest httpServletRequest);

    LoginResponse consumeLoginTicket(LoginTicketRequest request, HttpServletRequest httpServletRequest);

    boolean logout(HttpServletRequest httpServletRequest);

    AuthProfileResponse getProfile();
}
