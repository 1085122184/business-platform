package com.cjx.decision.dto.system.dingtalk;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DingTalk user candidate page.
 */
@Data
public class DingTalkUserPageResponse {

    private List<DingTalkUserCandidateResponse> list = new ArrayList<>();

    private Boolean hasMore;

    private Long nextCursor;
}
