package com.cjx.common.strategy.validator;

import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.common.strategy.metadata.StrategyMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 策略校验器
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class StrategyValidator {

    /**
     * 执行前校验
     */
    public void validate(StrategyMetadata metadata, StrategyContext context) {
        // 1. 校验认证 todo
//        validateAuth(metadata, context);

        // 2. 校验权限
        validatePermissions(metadata, context);

        // 3. 校验租户（多租户场景）todo
//        validateTenant(metadata, context);

        log.debug("策略校验通过: strategyKey={}",metadata.getStrategyKey());
    }

    /**
     * 校验认证
     */
//    private void validateAuth(StrategyMetadata metadata, StrategyContext context) {
//        if (metadata.isRequireAuth() && !StringUtils.hasText(context.getUserId())) {
//            throw new StrategyException("UNAUTHORIZED", "用户未登录");
//        }
//    }

    /**
     * 校验权限
     */
    private void validatePermissions(StrategyMetadata metadata, StrategyContext context) {
        String[] requiredPermissions = metadata.getPermissions();
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return;
        }

        // 这里应该调用权限服务进行校验
        // 示例代码仅做演示
        for (String permission : requiredPermissions) {
            // if (!permissionService.hasPermission(context.getUserId(), permission)) {
            //     throw new StrategyException("FORBIDDEN", "权限不足: " + permission);
            // }
        }
    }

    /**
     * 校验租户
     */
//    private void validateTenant(StrategyMetadata metadata, StrategyContext context) {
//        // 多租户场景下的校验逻辑
//        if (StringUtils.hasText(context.getTenantId())) {
//            // 校验租户是否有效
//        }
//    }
}
