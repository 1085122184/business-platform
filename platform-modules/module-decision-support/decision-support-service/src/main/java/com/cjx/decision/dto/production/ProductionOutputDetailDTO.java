package com.cjx.decision.dto.production;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductionOutputDetailDTO {
    private String company;
    private String materialCode;
    private String materialName;
    private String materialGroup;
    private String materialGroupName;
    private String month;
    private String postingDate;
    private String workCenter;
    private BigDecimal output;
}
