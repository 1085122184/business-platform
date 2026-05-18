package com.cjx.decision.dto.production;

import lombok.Data;

@Data
public class ProductionEnergyOverviewDTO {
    private ProductionMetricDTO water;
    private ProductionMetricDTO electricity;
    private ProductionMetricDTO refrigeration;
    private ProductionMetricDTO steam;
    private ProductionMetricDTO naturalGas;
    private ProductionMetricDTO hydrogen;
    private ProductionMetricDTO pureWater;
}
