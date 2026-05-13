package com.cjx.decision.entity.frorcl.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Login log entity for SYS_LOGIN_LOG.
 */
@Getter
@Setter
@Entity
@Table(name = "SYS_LOGIN_LOG")
public class SysLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_login_log_seq")
    @SequenceGenerator(name = "sys_login_log_seq", sequenceName = "SEQ_SYS_LOGIN_LOG", allocationSize = 1)
    @Column(name = "LOG_ID")
    private Long id;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USERNAME", length = 50)
    private String username;

    @Column(name = "LOGIN_STATUS", nullable = false)
    private Integer loginStatus;

    @Column(name = "LOGIN_MESSAGE", length = 500)
    private String loginMessage;

    @Column(name = "LOGIN_IP", length = 64)
    private String loginIp;

    @Column(name = "USER_AGENT", length = 1000)
    private String userAgent;

    @Column(name = "LOGIN_TIME", nullable = false)
    private LocalDateTime loginTime;

    @PrePersist
    protected void prePersist() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
    }
}
