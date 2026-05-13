package com.cjx.decision.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 区域编码枚举
 * 用于区分国内和国外市场
 * 
 * @author system
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RegionCode {
    
    /**
     * 国内市场
     */
    DOMESTIC("10", "国内"),
    
    /**
     * 国外市场
     */
    INTERNATIONAL("20", "国外");
    
    /**
     * 区域代码
     */
    private final String code;
    
    /**
     * 区域名称
     */
    private final String name;
    
    /**
     * 根据名称获取代码
     * @param name 区域名称("国内"或"国外")
     * @return 对应的区域代码
     */
    public static String getCodeByName(String name) {
        for (RegionCode region : values()) {
            if (region.getName().equals(name)) {
                return region.getCode();
            }
        }
        return DOMESTIC.getCode(); // 默认返回国内
    }
    
    /**
     * 根据代码获取枚举
     * @param code 区域代码
     * @return 对应的枚举,如果不存在返回DOMESTIC
     */
    public static RegionCode fromCode(String code) {
        for (RegionCode region : values()) {
            if (region.getCode().equals(code)) {
                return region;
            }
        }
        return DOMESTIC;
    }
}
