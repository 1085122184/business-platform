package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "各公司三费对比数据")
public class CompanyComparisonDTO {
    @Schema(description = "公司名称列表（横轴）")
    private List<String> company;

    @Schema(description = "各公司销售费用（万元）")
    private List<BigDecimal> sales;

    @Schema(description = "各公司管理费用（万元）")
    private List<BigDecimal> management;

    @Schema(description = "各公司财务费用（万元）")
    private List<BigDecimal> finance;
}