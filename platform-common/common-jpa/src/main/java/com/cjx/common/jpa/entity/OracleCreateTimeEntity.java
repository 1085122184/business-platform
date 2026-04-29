package com.cjx.common.jpa.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Common Oracle relation-table base class that only maintains create time.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class OracleCreateTimeEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "CREATE_TIME", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @PrePersist
    protected void prePersistOracleCreateTime() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
