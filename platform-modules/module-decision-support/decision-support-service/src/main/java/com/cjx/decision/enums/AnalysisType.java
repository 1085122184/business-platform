package com.cjx.decision.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分析类型枚举
 * 用于区分月度分析和年度分析
 * 
 * @author system
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AnalysisType {
    
    /**
     * 月度分析
     */
    MONTH("month", "月度分析"),
    
    /**
     * 年度分析
     */
    YEAR("year", "年度分析");
    
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
     * @return 对应的枚举,如果不存在返回MONTH
     */
    public static AnalysisType fromCode(String code) {
        for (AnalysisType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return MONTH;
    }
}
