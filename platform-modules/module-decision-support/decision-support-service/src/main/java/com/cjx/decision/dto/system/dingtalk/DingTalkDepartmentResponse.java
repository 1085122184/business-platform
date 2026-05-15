package com.cjx.decision.dto.system.dingtalk;

import lombok.Data;

/**
 * DingTalk department node for system account import.
 */
@Data
public class DingTalkDepartmentResponse {

    private Long deptId;

    private Long parentId;

    private String name;

    private Boolean leaf;
}
