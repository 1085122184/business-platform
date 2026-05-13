package com.cjx.decision.dto.salesdetail;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductMetricDTO {
    private String productCode;
    private String productName;
    private BigDecimal value;       // 销售额或销量
    private Integer percentage;     // 占比百分比 (前端直接展示，例如 35)
    private String region;

    // 全参构造函数方便组装
    public ProductMetricDTO(String productName,String productCode, BigDecimal value, Integer percentage) {
        this.productCode = productCode;
        this.productName = productName;
        this.value = value;
        this.percentage = percentage;
    }
}
