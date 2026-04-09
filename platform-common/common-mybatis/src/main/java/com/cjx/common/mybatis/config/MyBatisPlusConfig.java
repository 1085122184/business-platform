package com.cjx.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.cjx.common.mybatis.handler.MyMetaObjectHandler;
import com.cjx.common.mybatis.injector.CustomSqlInjector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis Plus配置
 *
 * @author company
 * @date 2024-01-20
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class MyBatisPlusConfig {
    /**
     * MyBatis Plus拦截器
     * 功能：分页、乐观锁、防全表更新删除
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页分页条数限制（防止恶意查询）
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 溢出总页数后是否进行处理
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        log.info("MyBatis Plus拦截器配置完成");
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }

    /**
     * 自定义SQL注入器（扩展MyBatis Plus方法）
     */
    @Bean
    public CustomSqlInjector customSqlInjector() {
        return new CustomSqlInjector();
    }
}
