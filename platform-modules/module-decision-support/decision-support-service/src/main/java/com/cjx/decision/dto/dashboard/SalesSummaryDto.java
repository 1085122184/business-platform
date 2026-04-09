package com.cjx.decision.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesSummaryDto {
    private Double totalSales;
    private Double totalAmount;
    private Double totalCountBudget;
    private Double totalAmountBudget;
    private String companyName;
    private Double collection;
}
