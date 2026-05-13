package com.cjx.decision.dto.dashboard;

import lombok.Data;


@Data
public class DashboardMetricsDTO {
    private RawSalesMetric salesVolume;
    private RawSalesMetric salesAmount;
    private RawCollection collection;
}
