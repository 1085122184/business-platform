package com.cjx.decision.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售趋势的单日数据点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTrendPointDTO {
    /** 走势点日期 (例如 "2025-06-01") */
    private String date;
    /** 当日销量 */
    private BigDecimal volume;
    /** 当日单价 */
    private BigDecimal price;
}
