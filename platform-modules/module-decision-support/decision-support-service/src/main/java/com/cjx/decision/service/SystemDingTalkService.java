package com.cjx.decision.service;

import com.cjx.decision.dto.system.dingtalk.DingTalkDepartmentResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportRequest;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserPageResponse;

import java.util.List;

/**
 * DingTalk organization import service.
 */
public interface SystemDingTalkService {

    List<DingTalkDepartmentResponse> listDepartments(Long deptId);

    DingTalkUserPageResponse listUsers(Long deptId, Boolean includeChildren, Long cursor, Integer size);

    DingTalkUserImportResponse importUsers(DingTalkUserImportRequest request);
}
