package com.cjx.decision.dto.expense;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 费用结构占比数据DTO
 *
 * @author system
 * @version 1.0.0
 */
@Data
public class ExpenseStructureDTO {
    
    /**
     * 费用类型名称
     */
    private String name;
    
    /**
     * 费用金额（亿元）
     */
    private BigDecimal value;
    
    /**
     * 占比百分比
     */
    private BigDecimal percent;
}
