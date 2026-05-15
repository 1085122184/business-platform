package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.system.dingtalk.DingTalkDepartmentResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportRequest;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserPageResponse;
import com.cjx.decision.service.SystemDingTalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * DingTalk organization endpoints for system account import.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/dingtalk")
@Tag(name = "DingTalk Account Import", description = "DingTalk organization and user import endpoints")
public class SystemDingTalkController {

    private final SystemDingTalkService systemDingTalkService;

    @Operation(summary = "List DingTalk child departments")
    @GetMapping("/departments")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).LIST)")
    public Result<List<DingTalkDepartmentResponse>> listDepartments(
            @RequestParam(value = "deptId", required = false, defaultValue = "1") Long deptId
    ) {
        return Result.success(systemDingTalkService.listDepartments(deptId));
    }

    @Operation(summary = "List DingTalk users for import")
    @GetMapping("/users")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).LIST)")
    public Result<DingTalkUserPageResponse> listUsers(
            @RequestParam(value = "deptId", required = false, defaultValue = "1") Long deptId,
            @RequestParam(value = "includeChildren", required = false, defaultValue = "false") Boolean includeChildren,
            @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,
            @RequestParam(value = "size", required = false, defaultValue = "50") Integer size
    ) {
        return Result.success(systemDingTalkService.listUsers(deptId, includeChildren, cursor, size));
    }

    @Operation(summary = "Import DingTalk users as system accounts")
    @PostMapping("/users/import")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).ADD)")
    public Result<DingTalkUserImportResponse> importUsers(@Valid @RequestBody DingTalkUserImportRequest request) {
        return Result.success("导入完成", systemDingTalkService.importUsers(request));
    }
}
