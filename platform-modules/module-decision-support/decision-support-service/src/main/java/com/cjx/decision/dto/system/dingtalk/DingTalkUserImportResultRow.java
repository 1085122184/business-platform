package com.cjx.decision.dto.system.dingtalk;

import lombok.Data;

/**
 * DingTalk user import row result.
 */
@Data
public class DingTalkUserImportResultRow {

    private String dingUserId;

    private String name;

    private String action;

    private Long userId;

    private String message;
}
