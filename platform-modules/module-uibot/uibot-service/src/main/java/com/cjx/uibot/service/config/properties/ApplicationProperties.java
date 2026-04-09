package com.cjx.uibot.service.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 通用应用配置属性类
 * <p>
 * 统一管理应用所有配置信息，支持类型安全、参数校验、IDE自动提示
 * </p>
 *
 * @author cjx
 * @since 1.0.0
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    /**
     * 触发器相关配置
     */
    @Valid
    private Trigger trigger;

    @Data
    public static class Trigger{

        /**
         * IP地址
         */
        @Valid
        private Master master;

        @Data
        public static class Master{

            /**
             * IP地址
             */
            @NotBlank(message = "IP地址不能为空")
            private String url;

            /**
             * 路径
             */
            @Valid
            private Path path;

            @Data
            public static class Path{
                /**
                 * 启动路径
                 */
                @NotBlank(message = "启动路径不能为空")
                private String start;
            }

        }
    }
}


