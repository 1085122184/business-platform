package com.cjx.decision.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawOrder {
    private String orderTitle;
    private String orderCount;
    private BigDecimal orderRate;
    private String barColor;
}
