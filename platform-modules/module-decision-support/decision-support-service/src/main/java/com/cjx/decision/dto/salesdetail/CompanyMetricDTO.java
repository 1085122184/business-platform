package com.cjx.decision.dto.salesdetail;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售明细公司卡片
 * @author cuijixu
 */
@Data
public class CompanyMetricDTO {
    private String companyName;
    private BigDecimal value;
    private BigDecimal target;
    private String ratioText;
    private Boolean isAlert;
    private List<BigDecimal> trend;
}
