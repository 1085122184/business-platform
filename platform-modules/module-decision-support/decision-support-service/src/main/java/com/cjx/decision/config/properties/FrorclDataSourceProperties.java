package com.cjx.decision.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource.frorcl") // 对应配置文件中的前缀
@Data
public class FrorclDataSourceProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;

    private Hikari hikari = new Hikari(); // 嵌套属性


    // 嵌套类用于Hikari配置
    @Data
    public static class Hikari {
        private int minimumIdle = 3;
        private int maximumPoolSize = 10;
    }
}
