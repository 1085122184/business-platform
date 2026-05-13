package com.cjx.decision.dto.system.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Query parameters for user paging.
 */
@Data
public class UserQueryRequest {

    @NotNull(message = "pageNum must not be null")
    @Min(value = 1, message = "pageNum must be greater than 0")
    private Integer pageNum;

    @NotNull(message = "pageSize must not be null")
    @Min(value = 1, message = "pageSize must be greater than 0")
    private Integer pageSize;

    private String username;

    private String mobile;

    private Integer status;
}
