package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionStartupShutdownOverviewDTO {
    private BigDecimal startupCount;
    private BigDecimal shutdownCount;
    private BigDecimal totalCount;
    private String unit;
}
