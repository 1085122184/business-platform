package com.cjx.common.jpa.repository.impl;

import com.cjx.common.jpa.entity.BaseEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 基础Repository实现
 * 扩展JPA的默认实现，添加批量操作优化
 *
 * @param <T> 实体类型
 * @author company
 * @date 2024-01-20
 */
public class BaseRepositoryImpl<T extends BaseEntity> extends SimpleJpaRepository<T, Long> {
    private final EntityManager entityManager;

    public BaseRepositoryImpl(JpaEntityInformation<T, Long> entityInformation,
                              EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    /**
     * 批量插入优化
     * 使用JDBC批处理，性能提升显著
     */
    @Transactional(rollbackFor = Exception.class)
    public <S extends T> List<S> batchSave(Iterable<S> entities) {
        int i = 0;
        for (S entity : entities) {
            entityManager.persist(entity);
            i++;
            // 每20条flush一次，避免内存溢出
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
        return (List<S>) entities;
    }

    /**
     * 批量更新优化
     */
    @Transactional(rollbackFor = Exception.class)
    public <S extends T> List<S> batchUpdate(Iterable<S> entities) {
        int i = 0;
        for (S entity : entities) {
            entityManager.merge(entity);
            i++;
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
        return (List<S>) entities;
    }
}
