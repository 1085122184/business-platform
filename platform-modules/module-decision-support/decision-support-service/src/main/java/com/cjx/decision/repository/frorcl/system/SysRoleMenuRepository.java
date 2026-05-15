package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysRoleMenu;
import com.cjx.decision.entity.frorcl.system.SysRoleMenuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SYS_ROLE_MENU.
 */
@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, SysRoleMenuId> {

    @Query("select rm.id.menuId from SysRoleMenu rm where rm.id.roleId = :roleId")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);

    boolean existsByIdRoleIdAndIdMenuId(Long roleId, Long menuId);

    @Modifying
    @Query("delete from SysRoleMenu rm where rm.id.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
