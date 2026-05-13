package com.cjx.common.security.service;

import com.cjx.common.core.utils.ThreadLocalUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import java.util.Set;

/**
 * 自定义权限校验服务
 * 在 @PreAuthorize 中使用 "@ss.hasPermi('xxx')" 调用
 */
@Service("ss")
public class PermissionService {

    private static final String ALL_PERMISSION = "*:*:*";

    public boolean hasPermi(String permission) {
        return hasPermission(permission);
    }

    public boolean hasPermission(String permission) {
        if (!StringUtils.hasText(permission)) {
            return false;
        }

        Set<String> permissions = ThreadLocalUtil.getPermissions();

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        return permissions.contains(ALL_PERMISSION) || hasAnyPermission(permissions, permission);
    }

    private boolean hasAnyPermission(Set<String> authorities, String permission) {
        return authorities.stream()
                .anyMatch(x -> ALL_PERMISSION.equals(x) || PatternMatchUtils.simpleMatch(x, permission));
    }
}
