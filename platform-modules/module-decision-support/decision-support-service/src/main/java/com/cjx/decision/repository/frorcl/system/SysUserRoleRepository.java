package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysUserRole;
import com.cjx.decision.entity.frorcl.system.SysUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SYS_USER_ROLE.
 */
@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, SysUserRoleId> {

    long countByIdRoleId(Long roleId);

    @Query("select ur.id.userId from SysUserRole ur where ur.id.roleId = :roleId")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);

    @Query("select ur.id.roleId from SysUserRole ur where ur.id.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    void deleteByIdUserId(Long userId);
}
