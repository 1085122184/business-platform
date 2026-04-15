package com.cjx.decision.dto.expense;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 三费趋势数据DTO（近12个月）
 *
 * @author system
 * @version 1.0.0
 */
@Data
public class ExpenseTrendDTO {
    
    /**
     * 月份数组
     */
    private List<String> months;
    
    /**
     * 销售费用数组（亿元）
     */
    private List<BigDecimal> sales;
    
    /**
     * 管理费用数组（亿元）
     */
    private List<BigDecimal> management;
    
    /**
     * 财务费用数组（亿元）
     */
    private List<BigDecimal> finance;
}
