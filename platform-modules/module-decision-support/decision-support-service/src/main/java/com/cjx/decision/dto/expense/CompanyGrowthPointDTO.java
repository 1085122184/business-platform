package com.cjx.decision.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 各公司三费同比环比增长数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGrowthPointDTO {
    private String companyName;
    private BigDecimal currentValue; // 本期值(万)
    private BigDecimal yoyValue;     // 去年同期值(万)
    private BigDecimal momValue;     // 上期环比值(万)
    private BigDecimal yoy;          // 同比增长率 %
    private BigDecimal mom;          // 环比增长率 %
}