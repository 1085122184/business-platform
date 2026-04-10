package com.cjx.decision.constant;

import java.util.Map;

/**
 * 公司编码常量类
 * 用于将公司名称映射到公司编码
 * 
 * @author system
 * @version 1.0.0
 */
public class CompanyCodeConstant {
    
    private CompanyCodeConstant() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * 月度表的公司名称映射(绿冷使用3001)
     */
    public static final Map<String, String> COMPANY_CODE_MAP_MONTH = Map.of(
        "绿冷", "3001",
        "有机硅", "1400",
        "氟硅", "1301",
        "高分子", "1201"
    );
    
    /**
     * 年度表的公司名称映射(绿冷使用3000)
     */
    public static final Map<String, String> COMPANY_CODE_MAP_YEAR = Map.of(
        "绿冷", "3000",
        "有机硅", "1400",
        "氟硅", "1300",
        "高分子", "1200"
    );
}
