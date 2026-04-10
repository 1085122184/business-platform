package com.cjx.decision.service.impl;

import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.projection.frorcl.CollectionDetail;
import com.cjx.decision.projection.frorcl.CollectionPlan;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.repository.frorcl.CollectionRepository;
import com.cjx.decision.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cuijixu
 */
@RequiredArgsConstructor
@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;

    @Override
    public List<CompanyMetricDTO> getCollectionCompanies(LocalDate date) {
        List<CompanyMetricDTO> result = new ArrayList<>();
        String yesterday = date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<CollectionDetail> companies = collectionRepository.findCollectionCompanies(yesterday);
        List<CollectionPlan> plans = collectionRepository.findCollectionPlan(yesterday);
        Map<String, BigDecimal> planMap = plans.stream()
                .collect(Collectors.toMap(CollectionPlan::getCompanyName,CollectionPlan::getPlanValue));
        companies.forEach(collectionDetail -> {
            CompanyMetricDTO companyMetricDTO = new CompanyMetricDTO();
            companyMetricDTO.setCompanyName(collectionDetail.getCompanyName());
            companyMetricDTO.setValue(collectionDetail.getValue());
            companyMetricDTO.setTarget(planMap.get(collectionDetail.getCompanyName()));
            result.add(companyMetricDTO);
        });
        return result;
    }
}
