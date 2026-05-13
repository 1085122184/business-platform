package com.cjx.decision.dto.system.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request body for creating a system user account.
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "username must not be blank")
    @Size(min = 2, max = 50, message = "username length must be between 2 and 50")
    private String username;

    @NotBlank(message = "password must not be blank")
    @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
    private String password;

    @Size(max = 100, message = "nickname length must not exceed 100")
    private String nickname;

    @Size(max = 100, message = "realName length must not exceed 100")
    private String realName;

    @Size(max = 100, message = "email length must not exceed 100")
    @Email(message = "email format is invalid")
    private String email;

    @Size(max = 30, message = "mobile length must not exceed 30")
    private String mobile;

    @NotNull(message = "status must not be null")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;

    @NotEmpty(message = "roleIds must not be empty")
    private List<Long> roleIds;
}
