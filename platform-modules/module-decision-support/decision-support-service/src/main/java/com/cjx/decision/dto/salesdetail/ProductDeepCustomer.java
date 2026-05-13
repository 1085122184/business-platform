package com.cjx.decision.dto.salesdetail;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductDeepCustomer {
    private String name;
    private BigDecimal volume;
    private BigDecimal amount;
}
