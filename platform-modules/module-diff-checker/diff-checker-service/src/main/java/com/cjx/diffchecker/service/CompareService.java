package com.cjx.diffchecker.service;

import java.math.BigDecimal;

/**
 * @author Administrator
 */
public interface CompareService {
    void compareTotal(String pkInvoice, BigDecimal srmNtax, BigDecimal srmNmny);
}
