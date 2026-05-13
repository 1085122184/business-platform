package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "各公司三费对比数据")
public class CompanyComparisonDTO {
    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "各公司销售费用（万元）")
    private BigDecimal sales;

    @Schema(description = "各公司管理费用（万元）")
    private BigDecimal management;

    @Schema(description = "各公司财务费用（万元）")
    private BigDecimal finance;

    @Schema(description = "各公司总费用（万元）")
    private BigDecimal total;

    @Schema(description = "各公司财务费用（万元）")
    private BigDecimal yoy;
}