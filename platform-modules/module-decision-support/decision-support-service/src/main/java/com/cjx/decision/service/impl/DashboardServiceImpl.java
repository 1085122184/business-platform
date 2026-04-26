package com.cjx.decision.service.impl;

import com.cjx.decision.dto.dashboard.*;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.projection.frorcl.AllDetails;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard服务门面实现
 * 委派请求到对应的子服务
 * 
 * @author system
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final MetricsService metricsService;
    private final PriceAnalysisService priceAnalysisService;
    private final SalesAnalysisService salesAnalysisService;
    private final TrendAnalysisService trendAnalysisService;
    private final CollectionService collectionService;
    private final OrderService orderService;
    private final AllDetailsService allDetailsService;


    @Override
    public DashboardMetricsDTO getMetrics(LocalDate date) {
        return metricsService.getMetrics(date);
    }
    
    @Override
    public DashboardOrdersDTO getOrders(LocalDate date) {
        return metricsService.getOrders(date);
    }
    
    @Override
    public List<RawPriceDeviation> getPriceDeviations(LocalDate date) {
        return priceAnalysisService.getPriceDeviations(date);
    }
    
    @Override
    public List<CustomerTransactionProjection> findCustomerTransaction(String region, String code, String type) {
        return priceAnalysisService.getCustomerTransactions(region, code,type);
    }
    
    @Override
    public List<CompanyMetricDTO> getCompanyList(String type, LocalDate date) {
        return salesAnalysisService.getCompanyList(type, date);
    }
    
    @Override
    public List<CompanyDetailDTO> getCompanyDetail(String companyName, String type, LocalDate date) {
        return salesAnalysisService.getCompanyDetail(companyName, type, date);
    }
    
    @Override
    public List<SalesTrendProductDTO> getSalesTrends(String date) {
        return trendAnalysisService.getMonthlyTrends(date);
    }
    
    @Override
    public List<SalesTrendPointDTO> getSalesTrendsList(String productCode, String region, String date) {
        return trendAnalysisService.getYearlyTrends(productCode, region, date);
    }
    
    @Override
    public ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date) {
        return trendAnalysisService.getProductDeepDetail(companyName, productCode, type, date);
    }

    @Override
    public List<CompanyMetricDTO> getCollectionCompanies(LocalDate date) {
        return collectionService.getCollectionCompanies(date);
    }

    @Override
    public List<OrderDetailDTO> getOrderCompanyDetails(String companyName, LocalDate date) {
        return orderService.getOrderWithDetails(date, companyName);
    }

    @Override
    public List<AllDetails> findSalesDetail(LocalDate targetDate, String companyName) {
        return allDetailsService.findSalesDetail(targetDate,companyName);
    }
}
