package com.cjx.decision.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawSalesMetric {
    private String metricName;
    private String displayValue;
    private String type;
    private BigDecimal budgetRate;
    private BigDecimal gapValue;
    private BigDecimal monthGoal;
}
