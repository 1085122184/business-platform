package com.cjx.decision.service;

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

import java.util.List;

/**
 * Role management service.
 */
public interface SystemRoleService {

    RolePageResponse pageRoles(RoleQueryRequest request);

    Long createRole(RoleSaveRequest request);

    Long createUser(UserCreateRequest request);

    UserPageResponse pageUsers(UserQueryRequest request);

    boolean updateUser(UserUpdateRequest request);

    boolean deleteUser(Long userId);

    boolean updateRole(RoleUpdateRequest request);

    boolean deleteRole(Long roleId);

    List<MenuTreeNodeResponse> getMenuTree();

    List<Long> getRoleMenuIds(Long roleId);

    boolean saveRoleMenus(Long roleId, List<Long> menuIds);

    PermissionAuditResponse auditRoutePermissions(PermissionAuditRequest request);

    PermissionAuditFixResponse fixMissingRoutePermissions(PermissionAuditRequest request);
}
