package com.cjx.decision.dto.production;

import lombok.Data;

@Data
public class ProductionOverviewDTO {
    private String date;
    private ProductionMetricDTO totalOutput;
    private ProductionMetricDTO rawMaterialConsumption;
    private ProductionMetricDTO productInventory;
    private ProductionMetricDTO outputMaterialGroups;
    private ProductionMetricDTO rawMaterialKinds;
    private ProductionThroughputDTO throughput;
    private ProductionEnergyOverviewDTO energy;
    private ProductionStartupShutdownOverviewDTO startupShutdown;
    private ProductionSafetyOverviewDTO safety;
    private ProductionEnvironmentOverviewDTO environment;
    private String latestInventoryTime;
}
