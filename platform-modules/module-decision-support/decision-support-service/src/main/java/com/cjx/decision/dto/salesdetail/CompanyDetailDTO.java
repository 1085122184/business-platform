package com.cjx.decision.dto.salesdetail;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompanyDetailDTO {
    private List<ProductMetricDTO> products; // 你的产品DTO保持不变
    private List<BigDecimal> dailySales;
}
