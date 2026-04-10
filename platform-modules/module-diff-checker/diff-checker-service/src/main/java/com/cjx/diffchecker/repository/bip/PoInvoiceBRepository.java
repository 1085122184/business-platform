package com.cjx.diffchecker.repository.bip;

import com.cjx.diffchecker.dto.InvoiceSummaryDTO;
import com.cjx.diffchecker.entity.bip.PoInvoiceB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Administrator
 */
@Repository
public interface PoInvoiceBRepository extends JpaRepository<PoInvoiceB, String> {

    @Query("SELECT new com.cjx.diffchecker.dto.InvoiceSummaryDTO(sum(i.ntax),sum(i.nmny)) " +
            "FROM PoInvoiceB i " +
            "WHERE i.pkInvoice = :pkInvoice")
    InvoiceSummaryDTO totalByPkInvoiceWithDto(@Param("pkInvoice") String pkInvoice);
}
