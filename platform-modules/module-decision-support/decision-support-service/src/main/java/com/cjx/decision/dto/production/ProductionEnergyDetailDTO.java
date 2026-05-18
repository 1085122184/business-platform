package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionEnergyDetailDTO {
    private String inputDate;
    private String postingDate;
    private String orderNo;
    private String factory;
    private BigDecimal water;
    private String waterUnit;
    private BigDecimal electricity;
    private String electricityUnit;
    private BigDecimal refrigeration;
    private String refrigerationUnit;
    private BigDecimal steam;
    private String steamUnit;
    private BigDecimal naturalGas;
    private String naturalGasUnit;
    private BigDecimal hydrogen;
    private String hydrogenUnit;
    private BigDecimal pureWater;
    private String pureWaterUnit;
}
