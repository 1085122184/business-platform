package com.cjx.decision.dto.production;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionMetricDTO {
    private String label;
    private BigDecimal value;
    private String unit;
    private String description;
}
