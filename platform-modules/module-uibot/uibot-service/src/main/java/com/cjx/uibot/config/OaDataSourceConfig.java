package com.cjx.uibot.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * 从数据源配置类
 * 管理secondary数据库的连接和JPA配置
 */
@Data
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties
@EnableJpaRepositories(
        basePackages = "com.cjx.uibot.repository.oa",  // 从数据源的Repository包路径
        entityManagerFactoryRef = "oaEntityManagerFactory",
        transactionManagerRef = "oaTransactionManager"
)
public class OaDataSourceConfig {
    @Value("${spring.datasource.oa.jdbc-url}")
    private String jdbcUrl;

    @Value("${spring.datasource.oa.username}")
    private String username;

    @Value("${spring.datasource.oa.password}")
    private String password;

    @Value("${spring.datasource.oa.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.oa.hikari.minimum-idle:3}")
    private int minimumIdle;

    @Value("${spring.datasource.oa.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Bean(name = "oaDataSource")
    public DataSource oaDataSource() {
        System.out.println("========== Creating oa DataSource ==========");
        System.out.println("JDBC URL: " + jdbcUrl);
        System.out.println("Username: " + username);
        System.out.println("Driver: " + driverClassName);
        System.out.println("===============================================");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        dataSource.setPoolName("oaHikariPool");
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setConnectionTestQuery("SELECT 1 FROM DUAL");

        return dataSource;
    }

    @Bean(name = "oaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean oaEntityManagerFactory(
            @Qualifier("oaDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.cjx.uibot.entity.oa");
        factory.setPersistenceUnitName("oaPersistenceUnit");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        factory.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Bean(name = "oaTransactionManager")
    public PlatformTransactionManager oaTransactionManager(
            @Qualifier("oaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
//    /**
//     * 从数据源属性配置
//     */
//    @Bean(name = "uiBotDataSourceProperties")
//    @ConfigurationProperties("spring.datasource.ui-bot")
//    public DataSourceProperties uiBotDataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    /**
//     * 从数据源配置
//     * 使用HikariCP连接池
//     */
//    @Bean(name = "uiBotDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.ui-bot.hikari")
//    public DataSource uiBotDataSource(
//            @Qualifier("uiBotDataSourceProperties") DataSourceProperties properties) {
//        return properties.initializeDataSourceBuilder()
//                .type(HikariDataSource.class)
//                .build();
//    }
//
//    /**
//     * 从数据源EntityManagerFactory配置
//     * 注意：这里复用主数据源的 EntityManagerFactoryBuilder
//     */
//    @Bean(name = "uiBotEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean uiBotEntityManagerFactory(
//            @Qualifier("entityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
//            @Qualifier("uiBotDataSource") DataSource dataSource) {
//        return builder
//                .dataSource(dataSource)
//                .packages("dongyue.api.model.entity.uibot")  // 从数据源的Entity包路径
//                .persistenceUnit("uiBotPersistenceUnit")
//                .build();
//    }
//
//    /**
//     * 从数据源事务管理器配置
//     */
//    @Bean(name = "uiBotTransactionManager")
//    public PlatformTransactionManager uiBotTransactionManager(
//            @Qualifier("uiBotEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
//        return new JpaTransactionManager(Objects.requireNonNull(factory.getObject()));
//    }
}