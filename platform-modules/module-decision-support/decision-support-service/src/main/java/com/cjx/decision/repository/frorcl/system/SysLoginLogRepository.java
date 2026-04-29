package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SYS_LOGIN_LOG.
 */
@Repository
public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long> {
}
