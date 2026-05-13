package com.cjx.decision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Administrator
 */
@SpringBootApplication(scanBasePackages = {"com.cjx.decision","com.cjx.common.*"})
@EnableCaching
@EnableScheduling
@EnableAsync
public class DecisionSupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecisionSupportApplication.class, args);
    }

}
