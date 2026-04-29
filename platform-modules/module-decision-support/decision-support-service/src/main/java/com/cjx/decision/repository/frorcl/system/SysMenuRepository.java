package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository for SYS_MENU.
 */
@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    List<SysMenu> findByStatusOrderByOrderNumAscIdAsc(Integer status);

    long countByIdIn(Collection<Long> ids);

    @Query(value = """
            SELECT DISTINCT M.PERMS
            FROM SYS_USER_ROLE UR
            JOIN SYS_ROLE_MENU RM ON RM.ROLE_ID = UR.ROLE_ID
            JOIN SYS_MENU M ON M.MENU_ID = RM.MENU_ID
            JOIN SYS_ROLE R ON R.ROLE_ID = UR.ROLE_ID
            WHERE UR.USER_ID = :userId
              AND R.STATUS = 1
              AND M.STATUS = 1
              AND M.PERMS IS NOT NULL
            ORDER BY M.PERMS
            """, nativeQuery = true)
    List<String> findPermissionsByUserId(@Param("userId") Long userId);
}
