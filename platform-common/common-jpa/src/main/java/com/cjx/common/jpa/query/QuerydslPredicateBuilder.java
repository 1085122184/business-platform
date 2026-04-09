package com.cjx.common.jpa.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Querydsl动态查询构建器
 * 提供类型安全的查询条件构建
 *
 * @author company
 * @date 2024-01-20
 */
@Slf4j
public class QuerydslPredicateBuilder {
    private final BooleanBuilder builder = new BooleanBuilder();

    /**
     * 创建构建器实例
     */
    public static QuerydslPredicateBuilder create() {
        return new QuerydslPredicateBuilder();
    }

    /**
     * 等于（处理null值）
     */
    public <T> QuerydslPredicateBuilder eq(SimpleExpression<T> path, T value) {
        if (value != null) {
            builder.and(path.eq(value));
        }
        return this;
    }

    /**
     * 不等于
     */
    public <T> QuerydslPredicateBuilder ne(SimpleExpression<T> path, T value) {
        if (value != null) {
            builder.and(path.ne(value));
        }
        return this;
    }

    /**
     * 模糊查询（包含）
     */
    public QuerydslPredicateBuilder like(StringPath path, String value) {
        if (value != null && !value.isEmpty()) {
            builder.and(path.like("%" + value + "%"));
        }
        return this;
    }

    /**
     * 左模糊查询
     */
    public QuerydslPredicateBuilder likeLeft(StringPath path, String value) {
        if (value != null && !value.isEmpty()) {
            builder.and(path.like("%" + value));
        }
        return this;
    }

    /**
     * 右模糊查询
     */
    public QuerydslPredicateBuilder likeRight(StringPath path, String value) {
        if (value != null && !value.isEmpty()) {
            builder.and(path.like(value + "%"));
        }
        return this;
    }

    /**
     * IN查询
     */
    public <T> QuerydslPredicateBuilder in(SimpleExpression<T> path, Collection<T> values) {
        if (values != null && !values.isEmpty()) {
            builder.and(path.in(values));
        }
        return this;
    }

    /**
     * NOT IN查询
     */
    public <T> QuerydslPredicateBuilder notIn(SimpleExpression<T> path, Collection<T> values) {
        if (values != null && !values.isEmpty()) {
            builder.and(path.notIn(values));
        }
        return this;
    }

    /**
     * 大于
     */
    public <T extends Comparable> QuerydslPredicateBuilder gt(ComparableExpression<T> path, T value) {
        if (value != null) {
            builder.and(path.gt(value));
        }
        return this;
    }

    /**
     * 大于等于
     */
    public <T extends Comparable> QuerydslPredicateBuilder goe(ComparableExpression<T> path, T value) {
        if (value != null) {
            builder.and(path.goe(value));
        }
        return this;
    }

    /**
     * 小于
     */
    public <T extends Comparable> QuerydslPredicateBuilder lt(ComparableExpression<T> path, T value) {
        if (value != null) {
            builder.and(path.lt(value));
        }
        return this;
    }

    /**
     * 小于等于
     */
    public <T extends Comparable> QuerydslPredicateBuilder loe(ComparableExpression<T> path, T value) {
        if (value != null) {
            builder.and(path.loe(value));
        }
        return this;
    }

    /**
     * 时间范围查询
     */
    public QuerydslPredicateBuilder between(DateTimePath<LocalDateTime> path,
                                            LocalDateTime start,
                                            LocalDateTime end) {
        if (start != null && end != null) {
            builder.and(path.between(start, end));
        } else if (start != null) {
            builder.and(path.goe(start));
        } else if (end != null) {
            builder.and(path.loe(end));
        }
        return this;
    }

    /**
     * IS NULL
     */
    public <T> QuerydslPredicateBuilder isNull(SimpleExpression<T> path) {
        builder.and(path.isNull());
        return this;
    }

    /**
     * IS NOT NULL
     */
    public <T> QuerydslPredicateBuilder isNotNull(SimpleExpression<T> path) {
        builder.and(path.isNotNull());
        return this;
    }

    /**
     * 自定义条件
     */
    public QuerydslPredicateBuilder and(Predicate predicate) {
        if (predicate != null) {
            builder.and(predicate);
        }
        return this;
    }

    /**
     * OR条件
     */
    public QuerydslPredicateBuilder or(Predicate predicate) {
        if (predicate != null) {
            builder.or(predicate);
        }
        return this;
    }

    /**
     * 条件表达式（延迟执行）
     */
    public QuerydslPredicateBuilder condition(boolean condition, Supplier<Predicate> supplier) {
        if (condition) {
            builder.and(supplier.get());
        }
        return this;
    }

    /**
     * 构建最终的Predicate
     */
    public Predicate build() {
        return builder.getValue() != null ? builder : null;
    }
}
