package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SYS_USER.
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsernameAndDelFlag(String username, String delFlag);

    Optional<SysUser> findByIdAndDelFlag(Long id, String delFlag);
}
