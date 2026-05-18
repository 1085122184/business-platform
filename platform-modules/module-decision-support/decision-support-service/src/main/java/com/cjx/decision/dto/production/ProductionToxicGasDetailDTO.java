package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionToxicGasDetailDTO {
    private String alarmDate;
    private String company;
    private BigDecimal alarmCount;
}
