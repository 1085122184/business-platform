package com.cjx.diffchecker.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Administrator
 */
@SpringBootApplication(scanBasePackages = {"com.cjx.diffchecker.service","com.cjx.common.*"})
public class DiffCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiffCheckerApplication.class, args);
    }

}
