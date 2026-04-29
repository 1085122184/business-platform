package com.cjx.decision.dto.system.role;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Query parameters for role paging.
 */
@Data
public class RoleQueryRequest {

    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum最小为1")
    private Integer pageNum;

    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize最小为1")
    private Integer pageSize;

    private String roleName;

    private String roleKey;

    private Integer status;
}
