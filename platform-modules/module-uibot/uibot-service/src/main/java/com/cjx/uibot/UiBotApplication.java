package com.cjx.uibot;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 任务队列系统主应用类
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12  "com.cjx.common.jpa","com.cjx.common.strategy","com.cjx.common.web"
 */
@SpringBootApplication(scanBasePackages = {"com.cjx.uibot","com.cjx.common.*"})
//@EnableFeignClients
//@EnableScheduling
@EnableTransactionManagement
@OpenAPIDefinition(
        info = @Info(
                title = "任务队列系统 API",
                version = "1.0.0",
                description = "基于Spring Boot的任务排队系统，支持严格顺序执行和外部回调"
//                contact = @Contact(
//                        name = "Task Queue System",
//                        email = "support@taskqueue.com"
//                ),
//                license = @License(
//                        name = "Apache 2.0",
//                        url = "https://www.apache.org/licenses/LICENSE-2.0"
//                )
        )
)
public class UiBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiBotApplication.class, args);
    }

}
