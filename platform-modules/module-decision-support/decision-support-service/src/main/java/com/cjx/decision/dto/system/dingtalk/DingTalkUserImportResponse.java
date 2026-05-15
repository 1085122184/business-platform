package com.cjx.decision.dto.system.dingtalk;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DingTalk user batch import response.
 */
@Data
public class DingTalkUserImportResponse {

    private Integer createdCount = 0;

    private Integer updatedCount = 0;

    private Integer conflictCount = 0;

    private Integer failedCount = 0;

    private List<DingTalkUserImportResultRow> rows = new ArrayList<>();

    public void increaseCreatedCount() {
        createdCount++;
    }

    public void increaseUpdatedCount() {
        updatedCount++;
    }

    public void increaseConflictCount() {
        conflictCount++;
    }

    public void increaseFailedCount() {
        failedCount++;
    }
}
