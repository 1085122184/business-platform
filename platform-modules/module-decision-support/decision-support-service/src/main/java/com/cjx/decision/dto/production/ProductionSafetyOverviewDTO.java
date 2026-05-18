package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionSafetyOverviewDTO {
    private ProductionMetricDTO processAlarm;
    private ProductionMetricDTO equipmentAlarm;
    private ProductionMetricDTO highRiskWork;
    private ProductionMetricDTO toxicGasAlarm;
    private ProductionMetricDTO riskCount;
    private ProductionMetricDTO dealCount;
    private BigDecimal rectificationRate;
}
