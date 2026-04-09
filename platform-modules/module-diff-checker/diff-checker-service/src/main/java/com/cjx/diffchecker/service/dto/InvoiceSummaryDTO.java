package com.cjx.diffchecker.service.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cuijixu
 */
@Data
public class InvoiceSummaryDTO {

    private BigDecimal totalNtax;
    private BigDecimal totalNmny;

    public InvoiceSummaryDTO(BigDecimal totalNtax, BigDecimal totalNmny) {
        this.totalNtax = totalNtax;
        this.totalNmny = totalNmny;
    }
}
