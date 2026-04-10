package com.cjx.decision.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawCollection {
    private String collectionAmount;
    private BigDecimal collectionRate;
    private BigDecimal gapValue;
    private BigDecimal monthGoal;
}
