package com.cjx.common.jpa.auditor;

import com.cjx.common.core.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * JPA审计功能实现
 * 自动填充创建人和更新人
 *
 * @author company
 * @date 2024-01-20
 */
@Slf4j
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        // 从ThreadLocal获取当前用户ID
        Long userId = ThreadLocalUtil.getUserId();

        if (userId == null) {
            log.warn("当前用户ID为空，审计功能可能无法正常工作");
            return Optional.empty();
        }

        return Optional.of(userId);
    }
}
