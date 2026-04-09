package com.cjx.decision.dto.dashboard;

import lombok.Data;

@Data
public class RawSalesMetric {
    private String metricName;
    private String displayValue;
    private String type;
    private Double budgetRate;
    private Double gapValue;
    private Double monthGoal;
}
