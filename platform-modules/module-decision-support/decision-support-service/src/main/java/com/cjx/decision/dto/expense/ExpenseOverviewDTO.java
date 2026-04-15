package com.cjx.decision.dto.expense;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 三费总览指标DTO
 *
 * @author system
 * @version 1.0.0
 */
@Data
public class ExpenseOverviewDTO {
    
    /**
     * 三费总额
     */
    private ExpenseAmount totalExpense;
    
    /**
     * 销售费用
     */
    private ExpensePercent salesExpense;
    
    /**
     * 管理费用
     */
    private ExpensePercent managementExpense;
    
    /**
     * 财务费用
     */
    private ExpensePercent financeExpense;
    
    /**
     * 费用金额（带同比）
     */
    @Data
    public static class ExpenseAmount {
        /**
         * 金额数值
         */
        private BigDecimal amount;
        
        /**
         * 单位（亿/万）
         */
        private String unit;
        
        /**
         * 同比变化（正数表示上涨，负数表示下降）
         */
        private BigDecimal yoyChange;
        
        /**
         * 同比变化文本描述
         */
        private String yoyChangeText;
    }
    
    /**
     * 费用金额（带占比）
     */
    @Data
    public static class ExpensePercent {
        /**
         * 金额数值
         */
        private BigDecimal amount;
        
        /**
         * 单位（亿/万）
         */
        private String unit;
        
        /**
         * 占比百分比
         */
        private BigDecimal percent;
        
        /**
         * 同比变化（正数表示上涨，负数表示下降）
         */
        private BigDecimal yoyChange;
    }
}
