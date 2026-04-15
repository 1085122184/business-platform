package com.cjx.decision.dto.expense;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 各公司三费对比数据DTO
 *
 * @author system
 * @version 1.0.0
 */
@Data
public class CompanyComparisonDTO {
    
    /**
     * 公司名称
     */
    private String name;
    
    /**
     * 销售费用（亿元）
     */
    private BigDecimal sales;
    
    /**
     * 管理费用（亿元）
     */
    private BigDecimal management;
    
    /**
     * 财务费用（亿元）
     */
    private BigDecimal finance;
    
    /**
     * 三费合计（亿元）
     */
    private BigDecimal total;
    
    /**
     * 同比变化百分比
     */
    private BigDecimal yoy;
}
