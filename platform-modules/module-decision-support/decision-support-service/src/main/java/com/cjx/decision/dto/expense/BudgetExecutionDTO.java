package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "三费预算执行比")
public class BudgetExecutionDTO {



    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "销售费用")
    private BigDecimal salesActual;

    @Schema(description = "销售费用预算")
    private BigDecimal salesBudget;

    @Schema(description = "管理费用")
    private BigDecimal mgmtActual;

    @Schema(description = "管理费用预算")
    private BigDecimal mgmtBudget;

    @Schema(description = "财务费用")
    private BigDecimal finActual;

    @Schema(description = "财务费用预算")
    private BigDecimal finBudget;
}
