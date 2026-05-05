package com.cjx.decision.dto.salesdetail;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author cuijixu
 */
@Data
@NoArgsConstructor
public class ProductDeepTrend {
    private String date;
    private BigDecimal domesticVolume;
    private BigDecimal intlVolume;
    private BigDecimal amount;
    private BigDecimal domesticAmount;
    private BigDecimal intlAmount;

}
