package com.cjx.decision.dto.system.role;

import lombok.Data;

import java.util.List;

/**
 * Paged role response compatible with the frontend contract.
 */
@Data
public class RolePageResponse {

    private List<RoleResponse> list;

    private List<RoleResponse> rows;

    private long total;

    private int pageNum;

    private int pageSize;
}
