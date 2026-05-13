package com.cjx.decision.dto.auth;

import lombok.Data;

import java.util.List;

/**
 * Auth profile response payload.
 */
@Data
public class AuthProfileResponse {

    private LoginUserInfoResponse userInfo;

    private List<String> roles;

    private List<String> permissions;
}
