package com.cjx.decision.dto.auth;

import lombok.Data;

/**
 * Logged-in user info.
 */
@Data
public class LoginUserInfoResponse {

    private Long userId;

    private String username;

    private String nickname;

    private Integer status;
}
