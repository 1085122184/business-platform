package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionWasteDetailDTO {
    private String postingDate;
    private String companyCode;
    private String companyName;
    private String company;
    private String wasteCode;
    private String wasteName;
    private BigDecimal output;
}
