package com.cjx.decision.service;

import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;
import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.dashboard.SalesTrendProductDTO;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardMetricsDTO getMetrics(LocalDate date);

    DashboardOrdersDTO getOrders(LocalDate date);

    List<RawPriceDeviation> getPriceDeviations(LocalDate date);

    List<CustomerTransactionProjection> findCustomerTransaction(String region, String code);

    List<CompanyMetricDTO> getCompanyList(String type, LocalDate date);

    List<CompanyDetailDTO> getCompanyDetail(String companyName, String type, LocalDate date, String target);

    List<SalesTrendProductDTO> getSalesTrends(String date);

    List<SalesTrendPointDTO> getSalesTrendsList(String productCode, String region, String date);

    ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date);

    List<CompanyMetricDTO> getCollectionCompanies(LocalDate date);



}
