package com.cjx.uibot.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceSummaryDTO {

    private BigDecimal totalNtax;
    private BigDecimal totalNmny;

    public InvoiceSummaryDTO(BigDecimal totalNtax, BigDecimal totalNmny) {
        this.totalNtax = totalNtax;
        this.totalNmny = totalNmny;
    }
}
