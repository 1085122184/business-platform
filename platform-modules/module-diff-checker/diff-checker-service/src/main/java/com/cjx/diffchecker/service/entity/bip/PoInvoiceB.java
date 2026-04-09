package com.cjx.diffchecker.service.entity.bip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author cuijixu
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "PO_INVOICE_B")
public class PoInvoiceB {

    @Id
    @Column(name = "PK_INVOICE_B")
    private String pkInvoiceB;

    @Column(name = "PK_INVOICE")
    private String pkInvoice;

    /*
    * 本币税额
    */
    @Column(name = "NTAX")
    private BigDecimal  ntax;

    /*
     * 本币无税金额
     */
    @Column(name = "NMNY")
    private BigDecimal nmny;

    /*
     * 发票号
     */
    @Column(name = "VBILLCODE")
    private String vbillcode;


}