package com.cjx.decision.service.impl;

import com.cjx.decision.constant.CompanyCodeConstant;
import com.cjx.decision.projection.frorcl.AllDetails;
import com.cjx.decision.repository.frorcl.AllDetailsRepository;
import com.cjx.decision.service.AllDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RequiredArgsConstructor
@Service
public class AllDetailsServiceImpl implements AllDetailsService {
    private final AllDetailsRepository allDetailsRepository;

    @Override
    public List<AllDetails> findSalesDetail(LocalDate targetDate, String companyName) {
        String companyCode = CompanyCodeConstant.COMPANY_CODE_MAP.getOrDefault(companyName,companyName);
        String date = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println(123);
        return allDetailsRepository.findSalesDetail(date,companyCode);
    }
}
