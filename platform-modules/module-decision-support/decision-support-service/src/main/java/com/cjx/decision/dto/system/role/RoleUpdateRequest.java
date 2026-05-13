package com.cjx.decision.dto.system.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request body for updating a role.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateRequest extends RoleSaveRequest {

    @NotNull(message = "id不能为空")
    private Long id;
}
