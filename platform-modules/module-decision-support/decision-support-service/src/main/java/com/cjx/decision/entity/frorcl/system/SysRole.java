package com.cjx.decision.entity.frorcl.system;

import com.cjx.common.jpa.entity.OracleAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Role entity for SYS_ROLE.
 */
@Getter
@Setter
@Entity
@Table(name = "SYS_ROLE")
public class SysRole extends OracleAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_role_seq")
    @SequenceGenerator(name = "sys_role_seq", sequenceName = "SEQ_SYS_ROLE", allocationSize = 1)
    @Column(name = "ROLE_ID")
    private Long id;

    @Column(name = "ROLE_NAME", nullable = false, length = 60)
    private String roleName;

    @Column(name = "ROLE_KEY", nullable = false, length = 100)
    private String roleKey;

    @Column(name = "ROLE_SORT", nullable = false)
    private Integer roleSort;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "REMARK", length = 500)
    private String remark;
}
