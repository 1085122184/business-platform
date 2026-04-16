package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "三费占比结构")
public class ExpenseStructureDTO {
    @Schema(description = "结构数据列表")
    private List<StructureItem> list;

    @Data
    @AllArgsConstructor
    public static class StructureItem {
        @Schema(description = "费用类别", example = "销售费用")
        private String name;
        @Schema(description = "金额")
        private BigDecimal value;
        @Schema(description = "占比")
        private BigDecimal percent;
    }
}