package com.cjx.decision.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Login request body.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "username不能为空")
    @Size(min = 2, max = 50, message = "username长度必须在2到50之间")
    private String username;

    @NotBlank(message = "password不能为空")
    @Size(min = 6, max = 50, message = "password长度必须在6到50之间")
    private String password;
}
