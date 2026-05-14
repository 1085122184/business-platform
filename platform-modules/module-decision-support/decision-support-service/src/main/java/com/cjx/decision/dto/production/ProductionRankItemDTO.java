package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionRankItemDTO {
    private String code;
    private String name;
    private String factory;
    private String company;
    private String category;
    private String unit;
    private BigDecimal value;
    private BigDecimal inbound;
    private BigDecimal outbound;
    private BigDecimal vehicleCount;
    private Integer factoryCount;
    private String updateTime;
}
