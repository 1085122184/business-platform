package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionHighRiskWorkDetailDTO {
    private String workDate;
    private String companyName;
    private BigDecimal workCount;
}
