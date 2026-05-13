package com.cjx.decision.service;


import com.cjx.decision.projection.frorcl.AllDetails;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AllDetailsService {

    List<AllDetails> findSalesDetail(@Param("targetDate") LocalDate targetDate, @Param("companyCode") String companyName);

}
