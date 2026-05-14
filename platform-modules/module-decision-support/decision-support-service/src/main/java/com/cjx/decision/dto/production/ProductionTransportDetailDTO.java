package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionTransportDetailDTO {
    private String orderNo;
    private String businessNo;
    private String processType;
    private String plateNo;
    private String driver;
    private String driverPhone;
    private BigDecimal grossWeight;
    private BigDecimal tareWeight;
    private BigDecimal netWeight;
    private String materialName;
    private String weighingDate;
    private String customerName;
    private String carrier;
    private String companyName;
    private String category;
}
