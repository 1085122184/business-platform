package com.cjx.decision.dto.production;

import lombok.Data;

@Data
public class ProductionStartupShutdownDetailDTO {
    private String sourceRn;
    private String company;
    private String device;
    private String time;
    private String dev;
    private String standard;
    private String sort;
    private String value;
}
