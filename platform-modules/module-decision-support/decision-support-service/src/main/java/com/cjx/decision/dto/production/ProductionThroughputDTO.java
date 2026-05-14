package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionThroughputDTO {
    private BigDecimal inbound;
    private BigDecimal outbound;
    private BigDecimal vehicleCount;
    private String unit;
}
