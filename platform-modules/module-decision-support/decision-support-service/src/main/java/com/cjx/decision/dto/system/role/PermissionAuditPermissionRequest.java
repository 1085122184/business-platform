package com.cjx.decision.dto.system.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Permission item submitted by the frontend route registry.
 */
@Data
public class PermissionAuditPermissionRequest {

    @NotBlank(message = "permission不能为空")
    @Size(max = 200, message = "permission长度不能超过200")
    private String permission;

    @Size(max = 100, message = "label长度不能超过100")
    private String label;
}
