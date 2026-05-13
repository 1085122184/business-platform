package com.cjx.common.jpa.entity;

import com.cjx.common.core.utils.ThreadLocalUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Common Oracle audit base class without id or logical-delete fields.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class OracleAuditEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "CREATE_BY", length = 64)
    private String createBy;

    @Column(name = "CREATE_TIME", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column(name = "UPDATE_BY", length = 64)
    private String updateBy;

    @Column(name = "UPDATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @PrePersist
    protected void prePersistOracleAudit() {
        LocalDateTime now = LocalDateTime.now();
        String operator = resolveCurrentOperator();
        if (!StringUtils.hasText(createBy)) {
            createBy = operator;
        }
        if (!StringUtils.hasText(updateBy)) {
            updateBy = operator;
        }
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate
    protected void preUpdateOracleAudit() {
        updateTime = LocalDateTime.now();
        updateBy = resolveCurrentOperator();
    }

    protected String resolveCurrentOperator() {
        String username = ThreadLocalUtil.getUsername();
        if (StringUtils.hasText(username)) {
            return username;
        }
        Long userId = ThreadLocalUtil.getUserId();
        return userId != null ? String.valueOf(userId) : "system";
    }
}
