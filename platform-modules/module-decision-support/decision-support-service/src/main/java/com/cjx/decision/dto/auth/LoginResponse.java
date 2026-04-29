package com.cjx.decision.dto.auth;

import lombok.Data;

import java.util.List;

/**
 * Login response payload.
 */
@Data
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private LoginUserInfoResponse userInfo;

    private List<String> roles;

    private List<String> permissions;
}
