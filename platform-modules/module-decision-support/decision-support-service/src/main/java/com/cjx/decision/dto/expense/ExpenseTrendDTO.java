package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "三费趋势数据")
public class ExpenseTrendDTO {
    @Schema(description = "月份轴")
    private List<String> months;
    @Schema(description = "销售费用趋势")
    private List<BigDecimal> sales;
    @Schema(description = "管理费用趋势")
    private List<BigDecimal> management;
    @Schema(description = "财务费用趋势")
    private List<BigDecimal> finance;
}