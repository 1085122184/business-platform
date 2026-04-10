package com.cjx.diffchecker.config;

import com.cjx.diffchecker.config.properties.BipDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * 从数据源配置类
 * 管理secondary数据库的连接和JPA配置
 * @author Administrator
 */
@Data
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties
@EnableJpaRepositories(
        basePackages = "com.cjx.diffchecker.repository.bip",  // 从数据源的Repository包路径
        entityManagerFactoryRef = "bipEntityManagerFactory",
        transactionManagerRef = "bipTransactionManager"
)
@RequiredArgsConstructor
public class BipDataSourceConfig {

    private final BipDataSourceProperties sourceProperties;
    @Value("${app.database.schema}")
    private String schema;

    @Primary
    @Bean(name = "bipDataSource")
    public DataSource bipDataSource() {
        System.out.println("========== Creating bip DataSource ==========");
        System.out.println("JDBC URL: " + sourceProperties.getJdbcUrl());
        System.out.println("Username: " + sourceProperties.getUsername());
        System.out.println("Driver: " + sourceProperties.getDriverClassName());
        System.out.println("===============================================");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(sourceProperties.getJdbcUrl());
        dataSource.setUsername(sourceProperties.getUsername());
        dataSource.setPassword(sourceProperties.getPassword());
        dataSource.setDriverClassName(sourceProperties.getDriverClassName());

        dataSource.setPoolName("bipHikariPool");
        dataSource.setMinimumIdle(sourceProperties.getHikari().getMinimumIdle());
        dataSource.setMaximumPoolSize(sourceProperties.getHikari().getMaximumPoolSize());
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setConnectionTestQuery("SELECT 1 FROM DUAL");

        return dataSource;
    }

    @Primary
    @Bean(name = "bipEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bipEntityManagerFactory(
            @Qualifier("bipDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.cjx.diffchecker.entity.bip");
        factory.setPersistenceUnitName("bipPersistenceUnit");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        factory.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.default_schema", schema);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Primary
    @Bean(name = "bipTransactionManager")
    public PlatformTransactionManager bipTransactionManager(
            @Qualifier("bipEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
