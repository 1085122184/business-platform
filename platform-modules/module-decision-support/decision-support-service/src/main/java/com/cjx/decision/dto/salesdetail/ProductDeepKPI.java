package com.cjx.decision.dto.salesdetail;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author cuijixu
 */
@Data
@NoArgsConstructor
public class ProductDeepKPI {
    private BigDecimal totalVolume;
    private BigDecimal avgPrice;
    private String profitEst;
}
