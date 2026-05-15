package com.cjx.decision.dto.system.role;

import lombok.Data;

/**
 * Single permission audit issue.
 */
@Data
public class PermissionAuditIssueResponse {

    private String type;

    private String severity;

    private String routePath;

    private String routeTitle;

    private String menuName;

    private String permission;

    private Boolean fixable;

    private String message;
}
