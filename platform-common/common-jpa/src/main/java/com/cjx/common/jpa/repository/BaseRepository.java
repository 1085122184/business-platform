package com.cjx.common.jpa.repository;

import com.cjx.common.jpa.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 基础Repository接口
 * 提供通用的CRUD方法和逻辑删除支持
 *
 * @param <T> 实体类型
 * @author company
 * @date 2024-01-20
 */
@NoRepositoryBean
public interface BaseRepository <T extends BaseEntity> extends
        JpaRepository<T, String>,
        JpaSpecificationExecutor<T> {
    /**
     * 根据ID查询（排除已删除）
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = 0")
    Optional<T> findByIdAndNotDeleted(@Param("id") String id);

    /**
     * 查询所有（排除已删除）
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = 0")
    List<T> findAllNotDeleted();

    /**
     * 根据ID列表查询（排除已删除）
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.deleted = 0")
    List<T> findByIdsAndNotDeleted(@Param("ids") List<String> ids);

    /**
     * 逻辑删除
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = 1 WHERE e.id = :id")
    int logicDeleteById(@Param("id") String id);

    /**
     * 批量逻辑删除
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = 1 WHERE e.id IN :ids")
    int logicDeleteByIds(@Param("ids") List<String> ids);

    /**
     * 统计未删除记录数
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = 0")
    long countNotDeleted();
}
