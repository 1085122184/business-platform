package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SYS_USER.
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUsernameAndDelFlag(String username, String delFlag);

    Optional<SysUser> findByIdAndDelFlag(Long id, String delFlag);

    Optional<SysUser> findByDingUserIdAndDelFlag(String dingUserId, String delFlag);

    Optional<SysUser> findByMobileAndDelFlag(String mobile, String delFlag);

    boolean existsByUsernameAndDelFlag(String username, String delFlag);

    boolean existsByMobileAndDelFlag(String mobile, String delFlag);

    boolean existsByMobileAndDelFlagAndIdNot(String mobile, String delFlag, Long id);
}
