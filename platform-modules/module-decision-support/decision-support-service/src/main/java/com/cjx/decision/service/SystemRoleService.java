package com.cjx.decision.service;

import com.cjx.decision.dto.system.role.MenuTreeNodeResponse;
import com.cjx.decision.dto.system.role.RolePageResponse;
import com.cjx.decision.dto.system.role.RoleQueryRequest;
import com.cjx.decision.dto.system.role.RoleSaveRequest;
import com.cjx.decision.dto.system.role.RoleUpdateRequest;

import java.util.List;

/**
 * Role management service.
 */
public interface SystemRoleService {

    RolePageResponse pageRoles(RoleQueryRequest request);

    Long createRole(RoleSaveRequest request);

    boolean updateRole(RoleUpdateRequest request);

    boolean deleteRole(Long roleId);

    List<MenuTreeNodeResponse> getMenuTree();

    List<Long> getRoleMenuIds(Long roleId);

    boolean saveRoleMenus(Long roleId, List<Long> menuIds);
}
