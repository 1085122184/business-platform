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
    private BigDecimal domesticVolume;
    private BigDecimal intlVolume;
    private BigDecimal totalAmount;
    private BigDecimal domesticAmount;
    private BigDecimal intlAmount;
    private BigDecimal avgPrice;
    private BigDecimal domesticAvgPrice;
    private BigDecimal intlAvgPrice;
    private String profitEst;
}
