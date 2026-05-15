package com.cjx.decision.dto.system.role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Frontend route item used for database permission audit.
 */
@Data
public class PermissionAuditRouteRequest {

    @NotBlank(message = "path不能为空")
    @Size(max = 200, message = "path长度不能超过200")
    private String path;

    @Size(max = 100, message = "name长度不能超过100")
    private String name;

    @Size(max = 100, message = "title长度不能超过100")
    private String title;

    private Boolean menuPathRequired;

    @Valid
    private List<PermissionAuditPermissionRequest> permissions = new ArrayList<>();
}
