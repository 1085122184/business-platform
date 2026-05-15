package com.cjx.decision.dto.system.role;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Permission audit response.
 */
@Data
public class PermissionAuditResponse {

    private PermissionAuditSummaryResponse summary;

    private List<PermissionAuditRouteRequest> routes = new ArrayList<>();

    private List<PermissionAuditIssueResponse> issues = new ArrayList<>();
}
