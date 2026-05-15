package com.cjx.decision.repository.frorcl.system;

import com.cjx.decision.entity.frorcl.system.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository for SYS_ROLE.
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

    boolean existsByRoleKey(String roleKey);

    boolean existsByRoleKeyAndIdNot(String roleKey, Long id);

    long countByIdIn(Collection<Long> ids);

    java.util.Optional<SysRole> findByRoleKey(String roleKey);

    @Query(value = """
            SELECT R.ROLE_KEY
            FROM SYS_USER_ROLE UR
            JOIN SYS_ROLE R ON R.ROLE_ID = UR.ROLE_ID
            WHERE UR.USER_ID = :userId
              AND R.STATUS = 1
            ORDER BY R.ROLE_SORT ASC, R.ROLE_ID ASC
            """, nativeQuery = true)
    List<String> findRoleKeysByUserId(@Param("userId") Long userId);
}
