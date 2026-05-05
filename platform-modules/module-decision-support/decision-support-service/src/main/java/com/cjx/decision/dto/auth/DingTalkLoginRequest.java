package com.cjx.decision.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DingTalk passwordless login request body.
 */
@Data
public class DingTalkLoginRequest {

    @NotBlank(message = "authCode must not be blank")
    private String authCode;
}
