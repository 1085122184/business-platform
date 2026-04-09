package com.cjx.decision.service;


import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;

import java.time.LocalDate;
import java.util.List;

public interface CollectionService {
    List<CompanyMetricDTO> getCollectionCompanies(LocalDate date);

}
