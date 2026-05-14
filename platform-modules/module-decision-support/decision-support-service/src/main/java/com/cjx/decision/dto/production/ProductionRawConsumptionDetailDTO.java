package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionRawConsumptionDetailDTO {
    private String inputDate;
    private String postingDate;
    private String factory;
    private String factoryName;
    private String materialCode;
    private String materialName;
    private String materialGroup;
    private String materialGroupName;
    private String storageLocation;
    private String unit;
    private String workCenter;
    private String workCenterName;
    private String orderNo;
    private String batchNo;
    private BigDecimal quantity;
    private String productionPlan;
    private String materialDocument;
    private String movementType;
    private String movementTypeName;
    private String month;
}
