package com.cjx.decision.dto.system.dingtalk;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DingTalk user candidate shown before import.
 */
@Data
public class DingTalkUserCandidateResponse {

    private String dingUserId;

    private String dingUnionId;

    private String name;

    private String mobile;

    private String email;

    private String avatar;

    private String position;

    private List<Long> deptIdList = new ArrayList<>();

    private String importStatus;

    private Long existingUserId;

    private String existingUsername;

    private String conflictReason;
}
