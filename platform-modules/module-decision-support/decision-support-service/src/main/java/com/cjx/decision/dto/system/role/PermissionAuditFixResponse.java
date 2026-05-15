package com.cjx.decision.dto.system.role;

import lombok.Data;

/**
 * Result of inserting missing route permissions.
 */
@Data
public class PermissionAuditFixResponse {

    private Integer insertedCount;

    private Integer skippedCount;

    private PermissionAuditResponse audit;
}
