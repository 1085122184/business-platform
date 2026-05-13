package com.cjx.decision.dto.system.user;

import lombok.Data;

import java.util.List;

/**
 * Paged user response compatible with the frontend contract.
 */
@Data
public class UserPageResponse {

    private List<UserResponse> list;

    private List<UserResponse> rows;

    private long total;

    private int pageNum;

    private int pageSize;
}
