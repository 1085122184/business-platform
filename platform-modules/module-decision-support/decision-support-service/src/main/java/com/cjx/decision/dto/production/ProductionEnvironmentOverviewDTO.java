package com.cjx.decision.dto.production;

import lombok.Data;

@Data
public class ProductionEnvironmentOverviewDTO {
    private ProductionMetricDTO exhaustEmissionPoints;
    private ProductionMetricDTO wastewaterEmissionPoints;
    private ProductionMetricDTO totalWaterGasPoints;
    private ProductionMetricDTO hazardousWaste;
}
