package com.cjx.common.dingtalk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 钉钉配置
 *
 * @author claude
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ding-talk")
public class DingTalkConfig {
    /**
     * Corp ID for DingTalk JSAPI passwordless login.
     */
    private String corpId;

    /**
     * 企业ID
     */
    private String appKey;

    /**
     * 应用ID（移动端）
     */
    private String agentId;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 扫码登录应用ID
     */
    private String appId;

    /**
     * 扫码登录应用密钥
     */
    private String scanAppSecret;

    /**
     * 钉钉API地址
     */
    private String apiUrl = "https://oapi.dingtalk.com";

    /**
     * Browser OAuth redirect uri for desktop DingTalk users.
     */
    private String oauthRedirectUri;
}
