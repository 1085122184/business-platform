package com.cjx.decision.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指标类型枚举
 * 用于区分销量和销售额指标
 * 
 * @author system
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum MetricType {
    
    /**
     * 总销量
     */
    VOLUME("volume", "总销量"),
    
    /**
     * 总销售额
     */
    AMOUNT("amount", "总销售额");
    
    /**
     * 类型代码
     */
    private final String code;
    
    /**
     * 类型名称
     */
    private final String name;
    
    /**
     * 根据代码获取枚举
     * @param code 类型代码
     * @return 对应的枚举,如果不存在返回VOLUME
     */
    public static MetricType fromCode(String code) {
        for (MetricType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return VOLUME; // 默认返回VOLUME
    }
}
