package com.cjx.common.strategy.dto.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 策略执行请求
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExecuteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 策略Key（格式：业务域:操作，如user:create）
     */
    private String strategyKey;

    /**
     * 业务参数
     */
    private Map<String, Object> params;

    /**
     * 扩展参数
     */
    private Map<String, Object> extras;
}
