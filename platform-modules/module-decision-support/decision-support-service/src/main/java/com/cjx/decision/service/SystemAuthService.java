package com.cjx.decision.service;

import com.cjx.decision.dto.auth.AuthProfileResponse;
import com.cjx.decision.dto.auth.LoginRequest;
import com.cjx.decision.dto.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Authentication service.
 */
public interface SystemAuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest);

    boolean logout(HttpServletRequest httpServletRequest);

    AuthProfileResponse getProfile();
}
