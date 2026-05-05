package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.common.jpa.query.JpaQueryHelper;
import com.cjx.common.jpa.utils.EntityDtoConverter;
import com.cjx.decision.dto.system.role.MenuTreeNodeResponse;
import com.cjx.decision.dto.system.role.RolePageResponse;
import com.cjx.decision.dto.system.role.RoleQueryRequest;
import com.cjx.decision.dto.system.role.RoleResponse;
import com.cjx.decision.dto.system.role.RoleSaveRequest;
import com.cjx.decision.dto.system.role.RoleUpdateRequest;
import com.cjx.decision.dto.system.user.UserCreateRequest;
import com.cjx.decision.dto.system.user.UserPageResponse;
import com.cjx.decision.dto.system.user.UserQueryRequest;
import com.cjx.decision.dto.system.user.UserResponse;
import com.cjx.decision.dto.system.user.UserUpdateRequest;
import com.cjx.decision.entity.frorcl.system.SysMenu;
import com.cjx.decision.entity.frorcl.system.SysRole;
import com.cjx.decision.entity.frorcl.system.SysRoleMenu;
import com.cjx.decision.entity.frorcl.system.SysRoleMenuId;
import com.cjx.decision.entity.frorcl.system.SysUser;
import com.cjx.decision.entity.frorcl.system.SysUserRole;
import com.cjx.decision.entity.frorcl.system.SysUserRoleId;
import com.cjx.decision.repository.frorcl.system.SysMenuRepository;
import com.cjx.decision.repository.frorcl.system.SysRoleMenuRepository;
import com.cjx.decision.repository.frorcl.system.SysRoleRepository;
import com.cjx.decision.repository.frorcl.system.SysUserRepository;
import com.cjx.decision.repository.frorcl.system.SysUserRoleRepository;
import com.cjx.decision.service.SystemRoleService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Role management implementation.
 */
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "frorclTransactionManager")
public class SystemRoleServiceImpl implements SystemRoleService {

    private final SysRoleRepository sysRoleRepository;
    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository sysRoleMenuRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final CaffeineCacheService caffeineCacheService;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    private volatile Boolean userRoleTableAvailable;

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public RolePageResponse pageRoles(RoleQueryRequest request) {
        Pageable pageable = JpaQueryHelper.createPageable(
                request.getPageNum(),
                request.getPageSize(),
                Sort.by(Sort.Order.asc("roleSort"), Sort.Order.asc("id"))
        );

        Page<SysRole> page = sysRoleRepository.findAll(buildRoleSpecification(request), pageable);
        List<RoleResponse> items = EntityDtoConverter.convertToDtoList(page.getContent(), RoleResponse.class);

        RolePageResponse response = new RolePageResponse();
        response.setList(items);
        response.setRows(items);
        response.setTotal(page.getTotalElements());
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        return response;
    }

    @Override
    public Long createRole(RoleSaveRequest request) {
        validateRoleKeyForCreate(request.getRoleKey());

        SysRole role = new SysRole();
        applyRolePayload(role, request);
        SysRole saved = sysRoleRepository.save(role);
        return saved.getId();
    }

    @Override
    public Long createUser(UserCreateRequest request) {
        String username = request.getUsername().trim();
        if (sysUserRepository.existsByUsernameAndDelFlag(username, "0")) {
            throw new BusinessException("username already exists");
        }
        if (StringUtils.hasText(request.getMobile())
                && sysUserRepository.existsByMobileAndDelFlag(request.getMobile().trim(), "0")) {
            throw new BusinessException("mobile already exists");
        }

        Set<Long> roleIds = new LinkedHashSet<>(request.getRoleIds());
        validateRoleIds(roleIds);

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(trimToNull(request.getNickname()));
        user.setRealName(trimToNull(request.getRealName()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setMobile(trimToNull(request.getMobile()));
        user.setStatus(request.getStatus());
        user.setDelFlag("0");

        SysUser savedUser = sysUserRepository.save(user);
        List<SysUserRole> relations = roleIds.stream()
                .map(roleId -> {
                    SysUserRole relation = new SysUserRole();
                    relation.setId(new SysUserRoleId(savedUser.getId(), roleId));
                    return relation;
                })
                .toList();
        sysUserRoleRepository.saveAll(relations);
        return savedUser.getId();
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public UserPageResponse pageUsers(UserQueryRequest request) {
        Pageable pageable = JpaQueryHelper.createPageable(
                request.getPageNum(),
                request.getPageSize(),
                Sort.by(Sort.Order.desc("createTime"), Sort.Order.asc("id"))
        );
        Page<SysUser> page = sysUserRepository.findAll(buildUserSpecification(request), pageable);
        List<UserResponse> items = page.getContent().stream()
                .map(this::toUserResponse)
                .toList();

        UserPageResponse response = new UserPageResponse();
        response.setList(items);
        response.setRows(items);
        response.setTotal(page.getTotalElements());
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        return response;
    }

    @Override
    public boolean updateUser(UserUpdateRequest request) {
        SysUser user = sysUserRepository.findByIdAndDelFlag(request.getId(), "0")
                .orElseThrow(() -> new BusinessException("user does not exist"));
        if (StringUtils.hasText(request.getMobile())
                && sysUserRepository.existsByMobileAndDelFlagAndIdNot(request.getMobile().trim(), "0", request.getId())) {
            throw new BusinessException("mobile already exists");
        }

        Set<Long> roleIds = new LinkedHashSet<>(request.getRoleIds());
        validateRoleIds(roleIds);

        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setNickname(trimToNull(request.getNickname()));
        user.setRealName(trimToNull(request.getRealName()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setMobile(trimToNull(request.getMobile()));
        user.setStatus(request.getStatus());
        sysUserRepository.save(user);
        replaceUserRoles(user.getId(), roleIds);
        evictUserPermissionCache(user.getId());
        return true;
    }

    @Override
    public boolean deleteUser(Long userId) {
        SysUser user = sysUserRepository.findByIdAndDelFlag(userId, "0")
                .orElseThrow(() -> new BusinessException("user does not exist"));
        user.setDelFlag("1");
        sysUserRepository.save(user);
        sysUserRoleRepository.deleteByIdUserId(userId);
        evictUserPermissionCache(userId);
        return true;
    }

    @Override
    public boolean updateRole(RoleUpdateRequest request) {
        SysRole role = sysRoleRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("角色不存在"));
        validateRoleKeyForUpdate(request.getRoleKey(), request.getId());

        applyRolePayload(role, request);
        sysRoleRepository.save(role);
        evictRolePermissionCache(request.getId());
        return true;
    }

    @Override
    public boolean deleteRole(Long roleId) {
        SysRole role = sysRoleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        if (hasUserRoleTable() && sysUserRoleRepository.countByIdRoleId(roleId) > 0) {
            throw new BusinessException("该角色已绑定用户，无法删除");
        }

        evictRolePermissionCache(roleId);
        sysRoleMenuRepository.deleteByRoleId(roleId);
        sysRoleRepository.delete(role);
        return true;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public List<MenuTreeNodeResponse> getMenuTree() {
        List<SysMenu> menus = sysMenuRepository.findByStatusOrderByOrderNumAscIdAsc(1);
        Map<Long, MenuTreeNodeResponse> nodeMap = new LinkedHashMap<>();

        for (SysMenu menu : menus) {
            MenuTreeNodeResponse node = new MenuTreeNodeResponse();
            node.setId(menu.getId());
            node.setMenuName(menu.getMenuName());
            node.setParentId(menu.getParentId());
            nodeMap.put(node.getId(), node);
        }

        List<MenuTreeNodeResponse> roots = new ArrayList<>();
        for (MenuTreeNodeResponse node : nodeMap.values()) {
            Long parentId = node.getParentId();
            MenuTreeNodeResponse parent = parentId == null ? null : nodeMap.get(parentId);
            if (parent == null || parentId == 0L) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public List<Long> getRoleMenuIds(Long roleId) {
        ensureRoleExists(roleId);
        return sysRoleMenuRepository.findMenuIdsByRoleId(roleId);
    }

    @Override
    public boolean saveRoleMenus(Long roleId, List<Long> menuIds) {
        ensureRoleExists(roleId);

        Set<Long> distinctMenuIds = menuIds == null
                ? java.util.Collections.emptySet()
                : new LinkedHashSet<>(menuIds);

        validateMenuIds(distinctMenuIds);
        sysRoleMenuRepository.deleteByRoleId(roleId);

        if (!distinctMenuIds.isEmpty()) {
            List<SysRoleMenu> relations = distinctMenuIds.stream()
                    .map(menuId -> {
                        SysRoleMenu relation = new SysRoleMenu();
                        relation.setId(new SysRoleMenuId(roleId, menuId));
                        return relation;
                    })
                    .toList();
            sysRoleMenuRepository.saveAll(relations);
        }

        evictRolePermissionCache(roleId);
        return true;
    }

    private Specification<SysRole> buildRoleSpecification(RoleQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(request.getRoleName())) {
                predicates.add(cb.like(root.get("roleName"), "%" + request.getRoleName().trim() + "%"));
            }
            if (StringUtils.hasText(request.getRoleKey())) {
                predicates.add(cb.like(root.get("roleKey"), "%" + request.getRoleKey().trim() + "%"));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<SysUser> buildUserSpecification(UserQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("delFlag"), "0"));
            if (StringUtils.hasText(request.getUsername())) {
                predicates.add(cb.like(root.get("username"), "%" + request.getUsername().trim() + "%"));
            }
            if (StringUtils.hasText(request.getMobile())) {
                predicates.add(cb.like(root.get("mobile"), "%" + request.getMobile().trim() + "%"));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserResponse toUserResponse(SysUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRealName(user.getRealName());
        response.setEmail(user.getEmail());
        response.setMobile(user.getMobile());
        response.setDingUserId(user.getDingUserId());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        List<Long> roleIds = sysUserRoleRepository.findRoleIdsByUserId(user.getId());
        response.setRoleIds(roleIds);
        if (roleIds != null && !roleIds.isEmpty()) {
            response.setRoleNames(sysRoleRepository.findAllById(roleIds).stream()
                    .map(SysRole::getRoleName)
                    .toList());
        }
        return response;
    }

    private void applyRolePayload(SysRole role, RoleSaveRequest request) {
        role.setRoleName(request.getRoleName().trim());
        role.setRoleKey(request.getRoleKey().trim());
        role.setRoleSort(request.getRoleSort());
        role.setStatus(request.getStatus());
        role.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
    }

    private void validateRoleKeyForCreate(String roleKey) {
        if (sysRoleRepository.existsByRoleKey(roleKey.trim())) {
            throw new BusinessException("角色权限字符已存在");
        }
    }

    private void validateRoleKeyForUpdate(String roleKey, Long roleId) {
        if (sysRoleRepository.existsByRoleKeyAndIdNot(roleKey.trim(), roleId)) {
            throw new BusinessException("角色权限字符已存在");
        }
    }

    private void ensureRoleExists(Long roleId) {
        if (!sysRoleRepository.existsById(roleId)) {
            throw new BusinessException("角色不存在");
        }
    }

    private void validateMenuIds(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        long count = sysMenuRepository.countByIdIn(menuIds);
        if (count != menuIds.size()) {
            throw new BusinessException("提交的菜单数据包含无效ID");
        }
    }

    private void validateRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("roleIds must not be empty");
        }
        long count = sysRoleRepository.countByIdIn(roleIds);
        if (count != roleIds.size()) {
            throw new BusinessException("submitted roleIds contain invalid role");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void replaceUserRoles(Long userId, Set<Long> roleIds) {
        sysUserRoleRepository.deleteByIdUserId(userId);
        List<SysUserRole> relations = roleIds.stream()
                .map(roleId -> {
                    SysUserRole relation = new SysUserRole();
                    relation.setId(new SysUserRoleId(userId, roleId));
                    return relation;
                })
                .toList();
        sysUserRoleRepository.saveAll(relations);
    }

    private void evictUserPermissionCache(Long userId) {
        caffeineCacheService.remove(CacheType.USER_PERMISSIONS, "perms:" + userId);
    }

    private void evictRolePermissionCache(Long roleId) {
        if (!hasUserRoleTable()) {
            return;
        }
        List<Long> userIds = sysUserRoleRepository.findUserIdsByRoleId(roleId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            Set<String> permissions = new LinkedHashSet<>(sysMenuRepository.findPermissionsByUserId(userId));
            caffeineCacheService.put(CacheType.USER_PERMISSIONS, "perms:" + userId, permissions);
        }
    }

    private boolean hasUserRoleTable() {
        if (userRoleTableAvailable != null) {
            return userRoleTableAvailable;
        }
        Number count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM USER_TABLES WHERE TABLE_NAME = 'SYS_USER_ROLE'",
                Number.class
        );
        userRoleTableAvailable = count != null && count.intValue() > 0;
        return userRoleTableAvailable;
    }
}
