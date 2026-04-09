package com.cjx.common.jpa.query;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA查询辅助工具
 *
 * @author company
 * @date 2024-01-20
 */
public class JpaQueryHelper {
    /**
     * 创建分页对象
     */
    public static Pageable createPageable(int pageNum, int pageSize) {
        return PageRequest.of(pageNum - 1, pageSize);
    }

    /**
     * 创建分页对象（带排序）
     */
    public static Pageable createPageable(int pageNum, int pageSize, Sort sort) {
        return PageRequest.of(pageNum - 1, pageSize, sort);
    }

    /**
     * 创建排序对象
     */
    public static Sort createSort(Sort.Direction direction, String... properties) {
        return Sort.by(direction, properties);
    }

    /**
     * 将Spring的Sort转换为Querydsl的OrderSpecifier
     */
    public static OrderSpecifier<?>[] convertSort(Sort sort, Path<?> entityPath) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            try {
                Path<?> path = (Path<?>) entityPath.getClass()
                        .getField(property)
                        .get(entityPath);

                if (path instanceof ComparableExpressionBase) {
                    orderSpecifiers.add(new OrderSpecifier(direction, (ComparableExpressionBase<?>) path));
                }
            } catch (Exception e) {
                // 忽略无效的排序字段
            }
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
