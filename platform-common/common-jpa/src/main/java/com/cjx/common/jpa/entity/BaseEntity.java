package com.cjx.common.jpa.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA基础实体类
 * 包含公共字段：主键、创建时间、更新时间、创建人、更新人、逻辑删除
 *
 * @author company
 * @date 2024-01-20
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("主键ID")
    private String id;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    @Comment("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @CreatedBy
    @Column(name = "create_by", updatable = false)
    @Comment("创建人ID")
    private Long createBy;

    /**
     * 更新人ID
     */
    @LastModifiedBy
    @Column(name = "update_by")
    @Comment("更新人ID")
    private Long updateBy;

    /**
     * 逻辑删除标识（0:未删除 1:已删除）
     */
    @Column(name = "deleted", nullable = false)
    @Comment("逻辑删除标识")
    private Integer deleted = 0;

    /**
     * 预持久化回调
     */
    @PrePersist
    protected void prePersist() {
        if (this.deleted == null) {
            this.deleted = 0;
        }
    }
}
