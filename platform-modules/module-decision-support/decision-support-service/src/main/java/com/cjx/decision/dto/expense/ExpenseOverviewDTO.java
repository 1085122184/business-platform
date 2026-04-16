package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "三费总览指标")
public class ExpenseOverviewDTO {
    @Schema(description = "三费总额")
    private MetricDetail totalExpense;
    @Schema(description = "销售费用")
    private MetricDetail salesExpense;
    @Schema(description = "管理费用")
    private MetricDetail managementExpense;
    @Schema(description = "财务费用")
    private MetricDetail financeExpense;

    @Data
    public static class MetricDetail {
        @Schema(description = "金额")
        private BigDecimal amount;
        @Schema(description = "单位", example = "亿")
        private String unit = "亿";
        @Schema(description = "占比 (百分比)", example = "45.5")
        private BigDecimal percent;
        @Schema(description = "同比变动 (%)", example = "-5.2")
        private BigDecimal yoyChange;
        @Schema(description = "同比变动描述", example = "同比下降 ¥1.16亿")
        private String yoyChangeText;
    }
}