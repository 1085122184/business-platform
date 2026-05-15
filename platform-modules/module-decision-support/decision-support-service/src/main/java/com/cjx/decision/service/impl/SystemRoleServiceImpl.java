package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.common.jpa.query.JpaQueryHelper;
import com.cjx.common.jpa.utils.EntityDtoConverter;
import com.cjx.decision.dto.system.role.MenuTreeNodeResponse;
import com.cjx.decision.dto.system.role.PermissionAuditFixResponse;
import com.cjx.decision.dto.system.role.PermissionAuditIssueResponse;
import com.cjx.decision.dto.system.role.PermissionAuditPermissionRequest;
import com.cjx.decision.dto.system.role.PermissionAuditRequest;
import com.cjx.decision.dto.system.role.PermissionAuditResponse;
import com.cjx.decision.dto.system.role.PermissionAuditRouteRequest;
import com.cjx.decision.dto.system.role.PermissionAuditSummaryResponse;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Role management implementation.
 */
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "frorclTransactionManager")
public class SystemRoleServiceImpl implements SystemRoleService {

    private static final String ADMIN_ROLE_KEY = "system:admin";
    private static final long ROOT_PARENT_ID = 0L;
    private static final String ROUTE_PERMISSION_MENU_TYPE = "C";
    private static final int DEFAULT_MENU_STATUS = 1;
    private static final int DEFAULT_MENU_ORDER = 999;

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
            node.setMenuType(menu.getMenuType());
            node.setPath(menu.getPath());
            node.setComponent(menu.getComponent());
            node.setPerms(menu.getPerms());
            node.setIcon(menu.getIcon());
            node.setOrderNum(menu.getOrderNum());
            node.setRemark(menu.getRemark());
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

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public PermissionAuditResponse auditRoutePermissions(PermissionAuditRequest request) {
        return buildPermissionAuditResponse(normalizeAuditRoutes(request));
    }

    @Override
    public PermissionAuditFixResponse fixMissingRoutePermissions(PermissionAuditRequest request) {
        List<PermissionAuditRouteRequest> routes = normalizeAuditRoutes(request);
        PermissionAuditResponse beforeFix = buildPermissionAuditResponse(routes);
        Map<String, PermissionAuditRouteRequest> missingRoutes = new LinkedHashMap<>();

        for (PermissionAuditIssueResponse issue : beforeFix.getIssues()) {
            if ("missing-permission".equals(issue.getType())
                    && Boolean.TRUE.equals(issue.getFixable())
                    && StringUtils.hasText(issue.getPermission())) {
                findRouteByPermission(routes, issue.getPermission())
                        .ifPresent(route -> missingRoutes.put(issue.getPermission(), route));
            }
        }

        int insertedCount = 0;
        SysMenu parent = ensureRoutePermissionRootMenu();
        Optional<SysRole> adminRole = sysRoleRepository.findByRoleKey(ADMIN_ROLE_KEY);
        Set<String> existingPaths = sysMenuRepository.findByStatusOrderByOrderNumAscIdAsc(DEFAULT_MENU_STATUS).stream()
                .map(SysMenu::getPath)
                .map(this::normalizePath)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        for (Map.Entry<String, PermissionAuditRouteRequest> entry : missingRoutes.entrySet()) {
            String permission = entry.getKey();
            if (sysMenuRepository.existsByPerms(permission)) {
                continue;
            }

            PermissionAuditRouteRequest route = entry.getValue();
            SysMenu menu = new SysMenu();
            menu.setParentId(parent.getId());
            menu.setMenuName(resolveMenuName(route, permission));
            String routePath = normalizePath(route.getPath());
            boolean canUseRoutePath = StringUtils.hasText(routePath) && !existingPaths.contains(routePath);
            menu.setMenuType(canUseRoutePath ? ROUTE_PERMISSION_MENU_TYPE : "B");
            menu.setPath(canUseRoutePath ? routePath : null);
            menu.setComponent(null);
            menu.setPerms(permission);
            menu.setIcon(null);
            menu.setOrderNum(resolveNextOrderNum(parent.getId()));
            menu.setStatus(DEFAULT_MENU_STATUS);
            menu.setRemark("由权限体检自动补入：" + nullToFallback(route.getTitle(), route.getPath()));
            SysMenu saved = sysMenuRepository.save(menu);
            if (canUseRoutePath) {
                existingPaths.add(routePath);
            }
            adminRole.ifPresent(role -> grantMenuToRoleIfAbsent(role.getId(), saved.getId()));
            insertedCount++;
        }

        PermissionAuditFixResponse response = new PermissionAuditFixResponse();
        response.setInsertedCount(insertedCount);
        response.setSkippedCount(Math.max(0, missingRoutes.size() - insertedCount));
        response.setAudit(buildPermissionAuditResponse(routes));
        return response;
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

    private PermissionAuditResponse buildPermissionAuditResponse(List<PermissionAuditRouteRequest> routes) {
        Set<String> permissions = new LinkedHashSet<>();
        Set<String> paths = new LinkedHashSet<>();
        for (PermissionAuditRouteRequest route : routes) {
            paths.add(route.getPath());
            for (PermissionAuditPermissionRequest permission : route.getPermissions()) {
                permissions.add(permission.getPermission());
            }
        }

        List<SysMenu> permissionMenus = permissions.isEmpty()
                ? java.util.Collections.emptyList()
                : sysMenuRepository.findByPermsIn(permissions);
        List<SysMenu> pathMenus = paths.isEmpty()
                ? java.util.Collections.emptyList()
                : sysMenuRepository.findByPathIn(paths);
        List<SysMenu> allMenus = sysMenuRepository.findByStatusOrderByOrderNumAscIdAsc(DEFAULT_MENU_STATUS);

        Map<String, List<SysMenu>> menusByPerms = groupByPermission(permissionMenus);
        Map<String, List<SysMenu>> menusByPath = groupByPath(pathMenus);
        Map<String, List<SysMenu>> allMenusByPath = groupByPath(allMenus);
        Set<String> routePaths = new LinkedHashSet<>(paths);
        List<PermissionAuditIssueResponse> issues = new ArrayList<>();

        for (PermissionAuditRouteRequest route : routes) {
            for (PermissionAuditPermissionRequest permission : route.getPermissions()) {
                if (!menusByPerms.containsKey(permission.getPermission())) {
                    issues.add(createIssue(
                            "missing-permission",
                            "error",
                            route.getPath(),
                            route.getTitle(),
                            null,
                            permission.getPermission(),
                            true,
                            nullToFallback(route.getTitle(), route.getPath())
                                    + " 使用的权限 " + permission.getPermission() + " 不存在于数据库 SYS_MENU"
                    ));
                }
            }

            if (Boolean.TRUE.equals(route.getMenuPathRequired()) && !menusByPath.containsKey(route.getPath())) {
                issues.add(createIssue(
                        "missing-route-path",
                        "warning",
                        route.getPath(),
                        route.getTitle(),
                        null,
                        route.getPermissions().stream()
                                .map(PermissionAuditPermissionRequest::getPermission)
                                .filter(StringUtils::hasText)
                                .findFirst()
                                .orElse(null),
                        false,
                        nullToFallback(route.getTitle(), route.getPath()) + " 未在数据库 SYS_MENU.PATH 中登记"
                ));
            }
        }

        for (Map.Entry<String, List<SysMenu>> entry : allMenusByPath.entrySet()) {
            String path = entry.getKey();
            List<SysMenu> menus = entry.getValue();
            if (menus.size() > 1) {
                issues.add(createIssue(
                        "duplicate-menu-path",
                        "warning",
                        path,
                        null,
                        menus.stream().map(SysMenu::getMenuName).filter(StringUtils::hasText).reduce((a, b) -> a + ", " + b).orElse(null),
                        null,
                        false,
                        "数据库 SYS_MENU.PATH " + path + " 存在 " + menus.size() + " 条记录"
                ));
            }
            boolean hasPageMenu = menus.stream().anyMatch(menu -> "C".equals(menu.getMenuType()));
            if (hasPageMenu && !"/".equals(path) && !routePaths.contains(path)) {
                issues.add(createIssue(
                        "orphan-menu-path",
                        "warning",
                        path,
                        null,
                        menus.stream().map(SysMenu::getMenuName).filter(StringUtils::hasText).reduce((a, b) -> a + ", " + b).orElse(null),
                        null,
                        false,
                        "数据库 SYS_MENU.PATH " + path + " 在当前前端受控路由中不存在"
                ));
            }
        }

        int errorCount = (int) issues.stream().filter(issue -> "error".equals(issue.getSeverity())).count();
        int warningCount = issues.size() - errorCount;
        int fixableCount = (int) issues.stream().filter(issue -> Boolean.TRUE.equals(issue.getFixable())).count();

        PermissionAuditSummaryResponse summary = new PermissionAuditSummaryResponse();
        summary.setProtectedRouteCount(routes.size());
        summary.setMenuPathCount(allMenusByPath.size());
        summary.setMenuPermissionCount(groupByPermission(allMenus).size());
        summary.setErrorCount(errorCount);
        summary.setWarningCount(warningCount);
        summary.setFixableCount(fixableCount);

        PermissionAuditResponse response = new PermissionAuditResponse();
        response.setSummary(summary);
        response.setRoutes(routes);
        response.setIssues(issues);
        return response;
    }

    private List<PermissionAuditRouteRequest> normalizeAuditRoutes(PermissionAuditRequest request) {
        Map<String, PermissionAuditRouteRequest> routeMap = new LinkedHashMap<>();
        if (request == null || request.getRoutes() == null) {
            return java.util.Collections.emptyList();
        }

        for (PermissionAuditRouteRequest route : request.getRoutes()) {
            if (route == null || !StringUtils.hasText(route.getPath())) {
                continue;
            }

            String path = normalizePath(route.getPath());
            PermissionAuditRouteRequest target = routeMap.computeIfAbsent(path, key -> {
                PermissionAuditRouteRequest item = new PermissionAuditRouteRequest();
                item.setPath(key);
                item.setName(trimToNull(route.getName()));
                item.setTitle(nullToFallback(trimToNull(route.getTitle()), key));
                item.setMenuPathRequired(Boolean.TRUE.equals(route.getMenuPathRequired()));
                return item;
            });

            Map<String, PermissionAuditPermissionRequest> permissionMap = new LinkedHashMap<>();
            for (PermissionAuditPermissionRequest existing : target.getPermissions()) {
                permissionMap.put(existing.getPermission(), existing);
            }

            if (route.getPermissions() != null) {
                for (PermissionAuditPermissionRequest permission : route.getPermissions()) {
                    if (permission == null || !StringUtils.hasText(permission.getPermission())) {
                        continue;
                    }
                    String permissionKey = permission.getPermission().trim();
                    permissionMap.computeIfAbsent(permissionKey, key -> {
                        PermissionAuditPermissionRequest item = new PermissionAuditPermissionRequest();
                        item.setPermission(key);
                        item.setLabel(nullToFallback(trimToNull(permission.getLabel()), nullToFallback(route.getTitle(), key)));
                        return item;
                    });
                }
            }

            target.setPermissions(new ArrayList<>(permissionMap.values()));
        }

        return routeMap.values().stream()
                .filter(route -> route.getPermissions() != null && !route.getPermissions().isEmpty())
                .toList();
    }

    private Map<String, List<SysMenu>> groupByPermission(List<SysMenu> menus) {
        Map<String, List<SysMenu>> result = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            if (!StringUtils.hasText(menu.getPerms())) {
                continue;
            }
            result.computeIfAbsent(menu.getPerms().trim(), key -> new ArrayList<>()).add(menu);
        }
        return result;
    }

    private Map<String, List<SysMenu>> groupByPath(List<SysMenu> menus) {
        Map<String, List<SysMenu>> result = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            String path = normalizePath(menu.getPath());
            if (!StringUtils.hasText(path)) {
                continue;
            }
            result.computeIfAbsent(path, key -> new ArrayList<>()).add(menu);
        }
        return result;
    }

    private Optional<PermissionAuditRouteRequest> findRouteByPermission(List<PermissionAuditRouteRequest> routes, String permission) {
        return routes.stream()
                .filter(route -> route.getPermissions().stream()
                        .anyMatch(item -> Objects.equals(item.getPermission(), permission)))
                .findFirst();
    }

    private PermissionAuditIssueResponse createIssue(
            String type,
            String severity,
            String routePath,
            String routeTitle,
            String menuName,
            String permission,
            Boolean fixable,
            String message
    ) {
        PermissionAuditIssueResponse issue = new PermissionAuditIssueResponse();
        issue.setType(type);
        issue.setSeverity(severity);
        issue.setRoutePath(routePath);
        issue.setRouteTitle(routeTitle);
        issue.setMenuName(menuName);
        issue.setPermission(permission);
        issue.setFixable(fixable);
        issue.setMessage(message);
        return issue;
    }

    private SysMenu ensureRoutePermissionRootMenu() {
        return sysMenuRepository.findFirstByMenuNameAndMenuTypeAndParentId("自动补入权限", "M", ROOT_PARENT_ID)
                .orElseGet(() -> {
                    SysMenu menu = new SysMenu();
                    menu.setParentId(ROOT_PARENT_ID);
                    menu.setMenuName("自动补入权限");
                    menu.setMenuType("M");
                    menu.setPath(null);
                    menu.setComponent(null);
                    menu.setPerms(null);
                    menu.setIcon(null);
                    menu.setOrderNum(DEFAULT_MENU_ORDER);
                    menu.setStatus(DEFAULT_MENU_STATUS);
                    menu.setRemark("权限体检自动创建的缺失权限目录");
                    return sysMenuRepository.save(menu);
                });
    }

    private String resolveMenuName(PermissionAuditRouteRequest route, String permission) {
        return route.getPermissions().stream()
                .filter(item -> Objects.equals(item.getPermission(), permission))
                .map(PermissionAuditPermissionRequest::getLabel)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElseGet(() -> nullToFallback(route.getTitle(), permission));
    }

    private Integer resolveNextOrderNum(Long parentId) {
        return sysMenuRepository.findByStatusOrderByOrderNumAscIdAsc(DEFAULT_MENU_STATUS).stream()
                .filter(menu -> Objects.equals(menu.getParentId(), parentId))
                .map(SysMenu::getOrderNum)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .map(order -> order + 1)
                .orElse(1);
    }

    private void grantMenuToRoleIfAbsent(Long roleId, Long menuId) {
        if (sysRoleMenuRepository.existsByIdRoleIdAndIdMenuId(roleId, menuId)) {
            return;
        }
        SysRoleMenu relation = new SysRoleMenu();
        relation.setId(new SysRoleMenuId(roleId, menuId));
        sysRoleMenuRepository.save(relation);
        evictRolePermissionCache(roleId);
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String normalized = path.trim().replaceAll("/+", "/");
        if ("/".equals(normalized)) {
            return normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String nullToFallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
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
