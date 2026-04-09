package com.cjx.uibot.service.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 主数据源配置类
 * 管理primary数据库的连接和JPA配置
 */
@Data
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.cjx.uibot.service.repository.dingtalk",  // 主数据源的Repository包路径
        entityManagerFactoryRef = "dingTalkEntityManagerFactory",
        transactionManagerRef = "dingTalkTransactionManager"
)
public class DingTalkDataSourceConfig {
    @Value("${spring.datasource.ding-talk.jdbc-url}")
    private String jdbcUrl;

    @Value("${spring.datasource.ding-talk.username}")
    private String username;

    @Value("${spring.datasource.ding-talk.password}")
    private String password;

    @Value("${spring.datasource.ding-talk.driver-class-name}")
    private String driverClassName;

    // HikariCP 配置（可选）
    @Value("${spring.datasource.ding-talk.hikari.minimum-idle:3}")
    private int minimumIdle;

    @Value("${spring.datasource.ding-talk.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.ding-talk.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    /**
     * 钉钉数据源配置
     * 直接使用 @Value 注入的值创建 DataSource
     */
    @Primary
    @Bean(name = "dingTalkDataSource")
    public DataSource dingTalkDataSource() {
        System.out.println("========== Creating DingTalk DataSource ==========");
        System.out.println("JDBC URL: " + jdbcUrl);
        System.out.println("Username: " + username);
        System.out.println("Driver: " + driverClassName);
        System.out.println("MinimumIdle: " + minimumIdle);
        System.out.println("MaximumPoolSize: " + maximumPoolSize);
        System.out.println("=================================================");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        // HikariCP 配置
        dataSource.setPoolName("DingTalkHikariPool");
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setConnectionTestQuery("SELECT 1");

        return dataSource;
    }
    /**
     * 钉钉数据源EntityManagerFactory配置
     */
    @Primary
    @Bean(name = "dingTalkEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dingTalkEntityManagerFactory(
            @Qualifier("dingTalkDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.cjx.uibot.service.entity.dingtalk");
        factory.setPersistenceUnitName("dingTalkPersistenceUnit");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        factory.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.jdbc.batch_size", 20);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    /**
     * 钉钉数据源事务管理器配置
     */
    @Primary
    @Bean(name = "dingTalkTransactionManager")
    public PlatformTransactionManager dingTalkTransactionManager(
            @Qualifier("dingTalkEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

//    /**
//     * 主数据源属性配置
//     */
//    @Primary
//    @Bean(name = "dingTalkDataSourceProperties")
//    @ConfigurationProperties("spring.datasource.ding-talk")
//    public DataSourceProperties dingTalkDataSourceProperties() {
//        DataSourceProperties properties = new DataSourceProperties();
//
//        // 添加日志输出
//        System.out.println("=== Primary DataSource Properties ===");
//        System.out.println("JDBC URL: " + properties.getUrl());
//        System.out.println("Username: " + properties.getUsername());
//        System.out.println("Driver: " + properties.getDriverClassName());
//        return new DataSourceProperties();
//    }
//
//    /**
//     * 主数据源配置
//     * 使用HikariCP连接池
//     */
//    @Primary
//    @Bean(name = "dingTalkDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.ding-talk.hikari")
//    public DataSource dingTalkDataSource(
//            @Qualifier("dingTalkDataSourceProperties") DataSourceProperties properties) {
//        return properties.initializeDataSourceBuilder()
//                .type(HikariDataSource.class)
//                .build();
//    }
//
//    /**
//     * 主数据源EntityManagerFactory配置
//     */
//    @Primary
//    @Bean(name = "dingTalkEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean dingTalkEntityManagerFactory(
//            @Qualifier("dingTalkDataSource") DataSource dataSource) {
//
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setPackagesToScan("com.example.entity.primary");  // Entity包路径
//        factory.setPersistenceUnitName("primaryPersistenceUnit");
//
//        // 配置HibernateJpaVendorAdapter
//        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//        vendorAdapter.setGenerateDdl(false);
//        factory.setJpaVendorAdapter(vendorAdapter);
//
//        // 配置Hibernate属性
//        Map<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
//        properties.put("hibernate.hbm2ddl.auto", "update");
//        properties.put("hibernate.show_sql", true);
//        properties.put("hibernate.format_sql", true);
//        factory.setJpaPropertyMap(properties);
//
//        return factory;
//    }
//
//    /**
//     * 主数据源事务管理器配置
//     */
//    @Primary
//    @Bean(name = "dingTalkTransactionManager")
//    public PlatformTransactionManager dingTalkTransactionManager(
//            @Qualifier("dingTalkEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
}
