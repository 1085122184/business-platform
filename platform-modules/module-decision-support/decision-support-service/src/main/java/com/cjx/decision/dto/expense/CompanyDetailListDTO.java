package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "公司三费明细列表")
public class CompanyDetailListDTO {
    private List<Item> list;
    private Long total;

    @Data
    public static class Item {
        @Schema(description = "公司名称")
        private String name;
        @Schema(description = "销售费用")
        private BigDecimal sales;
        @Schema(description = "管理费用")
        private BigDecimal management;
        @Schema(description = "财务费用")
        private BigDecimal finance;
        @Schema(description = "合计")
        private BigDecimal total;
        @Schema(description = "同比 (%)")
        private BigDecimal yoy;
    }
}