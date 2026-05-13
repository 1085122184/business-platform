package com.cjx.uibot.config;

import com.cjx.uibot.config.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 配置工具类
 * <p>提供便捷的配置访问方法</p>
 *
 * @author cjx
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigAccessor {
    private final ApplicationProperties properties;

    /**
     * 获取启动路径
     */
    public String getStartPath() {
        return properties.getTrigger().getMaster().getUrl()+properties.getTrigger().getMaster().getPath().getStart();
    }
}
