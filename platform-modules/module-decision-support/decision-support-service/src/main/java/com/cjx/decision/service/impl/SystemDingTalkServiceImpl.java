package com.cjx.decision.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.common.dingtalk.dto.DingTalkDeptInfo;
import com.cjx.common.dingtalk.dto.DingTalkUserInfo;
import com.cjx.common.dingtalk.dto.DingTalkUserPageResult;
import com.cjx.common.dingtalk.utils.DingTalkUtil;
import com.cjx.decision.dto.system.dingtalk.DingTalkDepartmentResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserCandidateResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportRequest;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportResponse;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserImportResultRow;
import com.cjx.decision.dto.system.dingtalk.DingTalkUserPageResponse;
import com.cjx.decision.entity.frorcl.system.SysUser;
import com.cjx.decision.entity.frorcl.system.SysUserRole;
import com.cjx.decision.entity.frorcl.system.SysUserRoleId;
import com.cjx.decision.repository.frorcl.system.SysRoleRepository;
import com.cjx.decision.repository.frorcl.system.SysUserRepository;
import com.cjx.decision.repository.frorcl.system.SysUserRoleRepository;
import com.cjx.decision.service.SystemDingTalkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * DingTalk organization import implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "frorclTransactionManager")
public class SystemDingTalkServiceImpl implements SystemDingTalkService {

    private static final long DEFAULT_ROOT_DEPT_ID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEL_FLAG_NORMAL = "0";
    private static final String ACTION_CREATED = "CREATED";
    private static final String ACTION_UPDATED = "UPDATED";
    private static final String ACTION_CONFLICT = "CONFLICT";
    private static final String ACTION_FAILED = "FAILED";

    private final DingTalkUtil dingTalkUtil;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaffeineCacheService caffeineCacheService;

    @Value("${system.user.dingtalk-import-default-password:}")
    private String defaultImportPassword;

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public List<DingTalkDepartmentResponse> listDepartments(Long deptId) {
        Long parentDeptId = deptId == null ? DEFAULT_ROOT_DEPT_ID : deptId;
        return caffeineCacheService.getOrLoadList(
                CacheType.DEFAULT,
                "dingtalk:dept:children:" + parentDeptId,
                key -> {
                    try {
                        List<DingTalkDeptInfo> departments = dingTalkUtil.listSubDepartments(parentDeptId);
                        List<DingTalkDepartmentResponse> responses = new ArrayList<>();
                        for (DingTalkDeptInfo department : departments) {
                            responses.add(toDepartmentResponse(department));
                        }
                        return responses;
                    } catch (Exception e) {
                        log.error("Failed to list DingTalk departments: deptId={}", parentDeptId, e);
                        throw new BusinessException("获取钉钉部门失败");
                    }
                }
        );
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "frorclTransactionManager")
    public DingTalkUserPageResponse listUsers(Long deptId, Boolean includeChildren, Long cursor, Integer size) {
        Long rootDeptId = deptId == null ? DEFAULT_ROOT_DEPT_ID : deptId;
        int pageSize = normalizePageSize(size);
        try {
            List<DingTalkUserInfo> users;
            boolean recursive = Boolean.TRUE.equals(includeChildren);
            Long nextCursor = null;
            boolean hasMore = false;
            if (recursive) {
                users = listUsersIncludingChildren(rootDeptId);
            } else {
                DingTalkUserPageResult page = dingTalkUtil.listDepartmentUsers(rootDeptId, cursor, pageSize);
                users = page.getList() == null ? List.of() : page.getList();
                hasMore = Boolean.TRUE.equals(page.getHasMore());
                nextCursor = page.getNextCursor();
            }

            DingTalkUserPageResponse response = new DingTalkUserPageResponse();
            response.setList(buildCandidateResponses(users));
            response.setHasMore(hasMore);
            response.setNextCursor(nextCursor);
            return response;
        } catch (Exception e) {
            log.error("Failed to list DingTalk users: deptId={}, includeChildren={}", rootDeptId, includeChildren, e);
            throw new BusinessException("获取钉钉人员失败");
        }
    }

    @Override
    public DingTalkUserImportResponse importUsers(DingTalkUserImportRequest request) {
        if (!StringUtils.hasText(defaultImportPassword)) {
            throw new BusinessException("未配置钉钉导入统一初始密码");
        }
        if (defaultImportPassword.length() < 6 || defaultImportPassword.length() > 64) {
            throw new BusinessException("钉钉导入统一初始密码长度必须为 6-64 位");
        }

        Set<Long> roleIds = new LinkedHashSet<>(request.getRoleIds());
        validateRoleIds(roleIds);

        DingTalkUserImportResponse response = new DingTalkUserImportResponse();
        Set<String> dingUserIds = normalizeDingUserIds(request.getDingUserIds());
        for (String dingUserId : dingUserIds) {
            DingTalkUserImportResultRow row = new DingTalkUserImportResultRow();
            row.setDingUserId(dingUserId);
            try {
                DingTalkUserInfo dingUser = dingTalkUtil.getUserDetail(dingUserId);
                row.setName(dingUser.getName());
                importSingleUser(dingUser, roleIds, request.getStatus(), row, response);
            } catch (BusinessException e) {
                row.setAction(ACTION_CONFLICT);
                row.setMessage(e.getMessage());
                response.increaseConflictCount();
            } catch (Exception e) {
                log.error("Failed to import DingTalk user: dingUserId={}", dingUserId, e);
                row.setAction(ACTION_FAILED);
                row.setMessage("导入失败");
                response.increaseFailedCount();
            }
            response.getRows().add(row);
        }
        return response;
    }

    private void importSingleUser(
            DingTalkUserInfo dingUser,
            Set<Long> roleIds,
            Integer status,
            DingTalkUserImportResultRow row,
            DingTalkUserImportResponse response
    ) {
        if (dingUser == null || !StringUtils.hasText(dingUser.getUserId())) {
            throw new BusinessException("钉钉用户信息不完整");
        }

        MatchResult dingMatched = findUniqueUserByDingUserId(dingUser.getUserId());
        MatchResult mobileMatched = StringUtils.hasText(dingUser.getMobile())
                ? findUniqueUserByMobile(dingUser.getMobile().trim())
                : MatchResult.empty();

        if (dingMatched.conflict()) {
            throw new BusinessException("多个账号命中同一钉钉用户");
        }
        if (mobileMatched.conflict()) {
            throw new BusinessException("多个账号命中同一手机号");
        }
        if (dingMatched.user().isPresent() && !"0".equals(dingMatched.user().get().getDelFlag())) {
            throw new BusinessException("已删除账号命中钉钉用户，不自动恢复");
        }
        if (mobileMatched.user().isPresent() && !"0".equals(mobileMatched.user().get().getDelFlag())) {
            throw new BusinessException("已删除账号命中手机号，不自动恢复");
        }
        if (dingMatched.user().isPresent()
                && mobileMatched.user().isPresent()
                && !dingMatched.user().get().getId().equals(mobileMatched.user().get().getId())) {
            throw new BusinessException("钉钉ID和手机号命中不同账号");
        }

        SysUser existing = dingMatched.user().orElseGet(() -> mobileMatched.user().orElse(null));
        if (existing == null) {
            SysUser user = new SysUser();
            applyDingTalkUser(user, dingUser, status);
            user.setUsername(resolveUsername(dingUser));
            user.setPasswordHash(passwordEncoder.encode(defaultImportPassword));
            user.setDelFlag(DEL_FLAG_NORMAL);
            SysUser saved = sysUserRepository.save(user);
            replaceUserRoles(saved.getId(), roleIds);
            row.setAction(ACTION_CREATED);
            row.setUserId(saved.getId());
            row.setMessage("导入成功");
            response.increaseCreatedCount();
            return;
        }

        applyDingTalkUser(existing, dingUser, status);
        SysUser saved = sysUserRepository.save(existing);
        replaceUserRoles(saved.getId(), roleIds);
        row.setAction(ACTION_UPDATED);
        row.setUserId(saved.getId());
        row.setMessage("覆盖更新成功");
        response.increaseUpdatedCount();
    }

    private List<DingTalkUserInfo> listUsersIncludingChildren(Long rootDeptId) throws Exception {
        Map<String, DingTalkUserInfo> users = new LinkedHashMap<>();
        Queue<Long> queue = new ArrayDeque<>();
        Set<Long> visitedDeptIds = new HashSet<>();
        queue.add(rootDeptId);

        while (!queue.isEmpty()) {
            Long deptId = queue.poll();
            if (deptId == null || !visitedDeptIds.add(deptId)) {
                continue;
            }

            Long cursor = 0L;
            boolean hasMore;
            do {
                DingTalkUserPageResult page = dingTalkUtil.listDepartmentUsers(deptId, cursor, MAX_PAGE_SIZE);
                if (page.getList() != null) {
                    for (DingTalkUserInfo user : page.getList()) {
                        if (user != null && StringUtils.hasText(user.getUserId())) {
                            users.putIfAbsent(user.getUserId(), user);
                        }
                    }
                }
                hasMore = Boolean.TRUE.equals(page.getHasMore());
                cursor = page.getNextCursor() == null ? 0L : page.getNextCursor();
            } while (hasMore);

            for (DingTalkDeptInfo child : dingTalkUtil.listSubDepartments(deptId)) {
                if (child != null && child.getDeptId() != null) {
                    queue.add(child.getDeptId());
                }
            }
        }

        return new ArrayList<>(users.values());
    }

    private List<DingTalkUserCandidateResponse> buildCandidateResponses(List<DingTalkUserInfo> users) {
        List<DingTalkUserCandidateResponse> responses = new ArrayList<>();
        for (DingTalkUserInfo user : users) {
            if (user == null || !StringUtils.hasText(user.getUserId())) {
                continue;
            }
            responses.add(toCandidateResponse(user));
        }
        return responses;
    }

    private DingTalkUserCandidateResponse toCandidateResponse(DingTalkUserInfo user) {
        DingTalkUserCandidateResponse response = new DingTalkUserCandidateResponse();
        response.setDingUserId(user.getUserId());
        response.setDingUnionId(user.getUnionId());
        response.setName(user.getName());
        response.setMobile(user.getMobile());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setPosition(user.getPosition());
        response.setDeptIdList(user.getDeptIdList() == null ? List.of() : user.getDeptIdList());

        MatchResult dingMatched = findUniqueUserByDingUserId(user.getUserId());
        MatchResult mobileMatched = StringUtils.hasText(user.getMobile())
                ? findUniqueUserByMobile(user.getMobile().trim())
                : MatchResult.empty();

        if (dingMatched.conflict()) {
            markConflict(response, "多个账号命中同一钉钉用户");
            return response;
        }
        if (mobileMatched.conflict()) {
            markConflict(response, "多个账号命中同一手机号");
            return response;
        }
        if (dingMatched.user().isPresent() && !"0".equals(dingMatched.user().get().getDelFlag())) {
            markConflict(response, "已删除账号命中钉钉用户");
            return response;
        }
        if (mobileMatched.user().isPresent() && !"0".equals(mobileMatched.user().get().getDelFlag())) {
            markConflict(response, "已删除账号命中手机号");
            return response;
        }
        if (dingMatched.user().isPresent()
                && mobileMatched.user().isPresent()
                && !dingMatched.user().get().getId().equals(mobileMatched.user().get().getId())) {
            markConflict(response, "钉钉ID和手机号命中不同账号");
            return response;
        }

        SysUser existing = dingMatched.user().orElseGet(() -> mobileMatched.user().orElse(null));
        if (existing == null) {
            response.setImportStatus("NEW");
            return response;
        }

        response.setImportStatus("EXISTS");
        response.setExistingUserId(existing.getId());
        response.setExistingUsername(existing.getUsername());
        return response;
    }

    private void markConflict(DingTalkUserCandidateResponse response, String reason) {
        response.setImportStatus("CONFLICT");
        response.setConflictReason(reason);
    }

    private DingTalkDepartmentResponse toDepartmentResponse(DingTalkDeptInfo department) throws Exception {
        DingTalkDepartmentResponse response = new DingTalkDepartmentResponse();
        response.setDeptId(department.getDeptId());
        response.setParentId(department.getParentId());
        response.setName(department.getName());
        response.setLeaf(dingTalkUtil.listSubDepartments(department.getDeptId()).isEmpty());
        return response;
    }

    private MatchResult findUniqueUserByDingUserId(String dingUserId) {
        if (!StringUtils.hasText(dingUserId)) {
            return MatchResult.empty();
        }
        return MatchResult.from(sysUserRepository.findAllByDingUserId(dingUserId.trim()));
    }

    private MatchResult findUniqueUserByMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return MatchResult.empty();
        }
        return MatchResult.from(sysUserRepository.findAllByMobile(mobile.trim()));
    }

    private void applyDingTalkUser(SysUser user, DingTalkUserInfo dingUser, Integer status) {
        String name = trimToNull(dingUser.getName());
        user.setNickname(name);
        user.setRealName(name);
        user.setEmail(trimToNull(dingUser.getEmail()));
        user.setMobile(trimToNull(dingUser.getMobile()));
        user.setDingUserId(trimToNull(dingUser.getUserId()));
        user.setDingUnionId(trimToNull(dingUser.getUnionId()));
        user.setStatus(status);
        user.setDelFlag(DEL_FLAG_NORMAL);
    }

    private String resolveUsername(DingTalkUserInfo dingUser) {
        if (StringUtils.hasText(dingUser.getMobile())) {
            String mobile = dingUser.getMobile().trim();
            if (!sysUserRepository.existsByUsername(mobile)) {
                return mobile;
            }
        }

        String baseUsername = "dt_" + DigestUtil.sha1Hex(dingUser.getUserId()).substring(0, 24);
        String username = baseUsername;
        int suffix = 1;
        while (sysUserRepository.existsByUsername(username)) {
            username = baseUsername + "_" + suffix++;
        }
        return username;
    }

    private int normalizePageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private Set<String> normalizeDingUserIds(List<String> dingUserIds) {
        Set<String> result = new LinkedHashSet<>();
        for (String dingUserId : dingUserIds) {
            if (StringUtils.hasText(dingUserId)) {
                result.add(dingUserId.trim());
            }
        }
        if (result.isEmpty()) {
            throw new BusinessException("dingUserIds must not be empty");
        }
        return result;
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record MatchResult(Optional<SysUser> user, boolean conflict) {
        static MatchResult empty() {
            return new MatchResult(Optional.empty(), false);
        }

        static MatchResult from(List<SysUser> users) {
            if (users == null || users.isEmpty()) {
                return empty();
            }
            if (users.size() > 1) {
                return new MatchResult(Optional.empty(), true);
            }
            return new MatchResult(Optional.of(users.get(0)), false);
        }
    }
}
