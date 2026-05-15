package com.cjx.decision.dto.system.role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for auditing frontend route permissions against SYS_MENU.
 */
@Data
public class PermissionAuditRequest {

    @Valid
    @NotEmpty(message = "routes不能为空")
    private List<PermissionAuditRouteRequest> routes = new ArrayList<>();
}
