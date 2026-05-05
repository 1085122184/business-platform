package com.cjx.decision.dto.system.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request body for updating a system user account.
 */
@Data
public class UserUpdateRequest {

    @NotNull(message = "id must not be null")
    private Long id;

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
