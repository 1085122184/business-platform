package com.cjx.decision.dto.dashboard;

import lombok.Data;

@Data
public class RawCollection {
    private String collectionAmount;
    private Double collectionRate;
    private Double gapValue;
    private Double monthGoal;
}
