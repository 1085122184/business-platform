package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.constant.SystemRolePermissions;
import com.cjx.decision.dto.system.role.MenuTreeNodeResponse;
import com.cjx.decision.dto.system.role.PermissionAuditFixResponse;
import com.cjx.decision.dto.system.role.PermissionAuditRequest;
import com.cjx.decision.dto.system.role.PermissionAuditResponse;
import com.cjx.decision.dto.system.role.RolePageResponse;
import com.cjx.decision.dto.system.role.RoleQueryRequest;
import com.cjx.decision.dto.system.role.RoleSaveRequest;
import com.cjx.decision.dto.system.role.RoleUpdateRequest;
import com.cjx.decision.dto.system.user.UserCreateRequest;
import com.cjx.decision.dto.system.user.UserPageResponse;
import com.cjx.decision.dto.system.user.UserQueryRequest;
import com.cjx.decision.dto.system.user.UserUpdateRequest;
import com.cjx.decision.service.SystemRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Role management endpoints.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/roles")
@Tag(name = "角色管理", description = "角色、菜单与授权管理接口")
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    @Operation(summary = "分页查询角色")
    @GetMapping
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).LIST)")
    public Result<RolePageResponse> pageRoles(@Valid RoleQueryRequest request) {
        return Result.success(systemRoleService.pageRoles(request));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).ADD)")
    public Result<Long> createRole(@Valid @RequestBody RoleSaveRequest request) {
        return Result.success("新增成功", systemRoleService.createRole(request));
    }

    @Operation(summary = "新增账号")
    @PostMapping("/users")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).ADD)")
    public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        return Result.success("新增成功", systemRoleService.createUser(request));
    }

    @Operation(summary = "分页查询账号")
    @GetMapping("/users")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).LIST)")
    public Result<UserPageResponse> pageUsers(@Valid UserQueryRequest request) {
        return Result.success(systemRoleService.pageUsers(request));
    }

    @Operation(summary = "修改账号")
    @PutMapping("/users")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).EDIT)")
    public Result<Boolean> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        return Result.success("修改成功", systemRoleService.updateUser(request));
    }

    @Operation(summary = "删除账号")
    @DeleteMapping("/users/{id}")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).REMOVE)")
    public Result<Boolean> deleteUser(@PathVariable("id") Long userId) {
        return Result.success("删除成功", systemRoleService.deleteUser(userId));
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).EDIT)")
    public Result<Boolean> updateRole(@Valid @RequestBody RoleUpdateRequest request) {
        return Result.success("修改成功", systemRoleService.updateRole(request));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).REMOVE)")
    public Result<Boolean> deleteRole(@PathVariable("id") Long roleId) {
        return Result.success("删除成功", systemRoleService.deleteRole(roleId));
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/menu-tree")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).LIST)")
    public Result<List<MenuTreeNodeResponse>> getMenuTree() {
        return Result.success(systemRoleService.getMenuTree());
    }

    @Operation(summary = "体检前端路由权限与数据库菜单")
    @PostMapping("/permission-audit")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).LIST)")
    public Result<PermissionAuditResponse> auditRoutePermissions(@Valid @RequestBody PermissionAuditRequest request) {
        return Result.success(systemRoleService.auditRoutePermissions(request));
    }

    @Operation(summary = "补入缺失的前端路由权限")
    @PostMapping("/permission-audit/fix")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).GRANT)")
    public Result<PermissionAuditFixResponse> fixMissingRoutePermissions(@Valid @RequestBody PermissionAuditRequest request) {
        return Result.success("补入成功", systemRoleService.fixMissingRoutePermissions(request));
    }

    @Operation(summary = "查询角色已分配菜单")
    @GetMapping("/{roleId}/menus")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).GRANT)")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return Result.success(systemRoleService.getRoleMenuIds(roleId));
    }

    @Operation(summary = "保存角色菜单权限")
    @PutMapping("/{roleId}/menus")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.SystemRolePermissions).GRANT)")
    public Result<Boolean> saveRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        return Result.success("保存成功", systemRoleService.saveRoleMenus(roleId, menuIds));
    }
}
