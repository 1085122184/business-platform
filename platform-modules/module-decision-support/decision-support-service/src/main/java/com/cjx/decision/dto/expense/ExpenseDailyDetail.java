package com.cjx.decision.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "三费每日详情")
public class ExpenseDailyDetail {

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "费用类型")
    private String types;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "行项目文本")
    private String text;
}
