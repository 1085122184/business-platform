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

import java.time.LocalDateTime;

/**
 * User entity for SYS_USER.
 */
@Getter
@Setter
@Entity
@Table(name = "SYS_USER")
public class SysUser extends OracleAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_user_seq")
    @SequenceGenerator(name = "sys_user_seq", sequenceName = "SEQ_SYS_USER", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "NICKNAME", length = 100)
    private String nickname;

    @Column(name = "REAL_NAME", length = 100)
    private String realName;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "MOBILE", length = 30)
    private String mobile;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "DEL_FLAG", nullable = false, length = 1)
    private String delFlag;

    @Column(name = "LAST_LOGIN_TIME")
    private LocalDateTime lastLoginTime;

    @Column(name = "LAST_LOGIN_IP", length = 64)
    private String lastLoginIp;
}
