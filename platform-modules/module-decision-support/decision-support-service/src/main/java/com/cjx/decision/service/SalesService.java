package com.cjx.decision.service;

import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.projection.frorcl.SalesSummary;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Administrator
 */
public interface SalesService {

    SalesSummary findSummaryByDate(LocalDate date);

    SalesSummary findSummaryToToday(LocalDate date);

    SalesSummary findCountBudget(LocalDate date);

    SalesSummary findAmountBudget(LocalDate dat);

    SalesSummary findMonthOrder(LocalDate targetDate);

    SalesSummary findYearOrder(LocalDate targetDate);

    SalesSummary findCollection(LocalDate date);

    List<RawPriceDeviation> findPriceDiff(LocalDate date);

    List<CustomerTransactionProjection> findCustomerTransaction(String region, String code,String targetDate);

    List<CompanyMetricDTO> findSaleDetails(String type, LocalDate targetDate);

    List<SalesSummary> findSummaryByCompany(LocalDate targetDate,String company);

    List<SalesSummary> findDetailsByCompany(LocalDate targetDate,String company);

    List<SalesSummary> findTrendsToday(String targetDate);

    List<SalesSummary> findTrendsAll(LocalDate endLocalDate);

    List<SalesSummary> findTrendsYear(LocalDate endLocalDate,String productCode,String region);

    List<SalesSummary> getProductDeepMonth(String companyName, String productCode, LocalDate date);

    List<SalesSummary> getProductDeepYear(String companyName, String productCode, LocalDate date);

    List<SalesSummary> getProductCustomer(String companyName, String productCode, LocalDate date);

}
