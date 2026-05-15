package com.cjx.decision.dto.system.role;

import lombok.Data;

/**
 * Permission audit summary.
 */
@Data
public class PermissionAuditSummaryResponse {

    private Integer protectedRouteCount;

    private Integer menuPathCount;

    private Integer menuPermissionCount;

    private Integer errorCount;

    private Integer warningCount;

    private Integer fixableCount;
}
