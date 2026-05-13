package com.cjx.common.jpa.config;

import com.cjx.common.jpa.auditor.AuditorAwareImpl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * JPA配置类
 * 只包含JPA功能配置，不包含数据源配置
 *
 * @author company
 * @date 2024-01-20
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    /**
     * 配置审计功能的当前用户提供者
     */
    @Bean
    public AuditorAware<Long> auditorAware() {
        log.info("初始化JPA审计功能");
        return new AuditorAwareImpl();
    }

    /**
     * 内部配置类
     * IDEA不会检查内部类的字段注入
     */
    @Configuration
    @ConditionalOnBean(EntityManager.class)
    static class QuerydslConfiguration {

        @PersistenceContext
        private EntityManager entityManager;

        @Bean
        @Lazy
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(entityManager);
        }
    }
}
