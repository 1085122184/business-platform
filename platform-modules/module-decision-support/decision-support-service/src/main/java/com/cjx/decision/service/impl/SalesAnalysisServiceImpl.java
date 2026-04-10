package com.cjx.decision.service.impl;

import com.cjx.decision.constant.CompanyCodeConstant;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductMetricDTO;
import com.cjx.decision.enums.MetricType;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.service.SalesAnalysisService;
import com.cjx.decision.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售分析服务实现
 * 负责查询销售明细和公司详情数据
 * 
 * @author system
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SalesAnalysisServiceImpl implements SalesAnalysisService {
    
    private final SalesService salesService;

    /**
     * 获取销售明细看板数据 (提供给前端直接使用)
     */
    @Override
    public List<CompanyMetricDTO> getCompanyList(String type, LocalDate date) {
        return salesService.findSaleDetails(type,date);
    }

    @Override
    public List<CompanyDetailDTO> getCompanyDetail(String companyName, String type, LocalDate date) {
        companyName = CompanyCodeConstant.COMPANY_CODE_MAP_MONTH.getOrDefault(companyName, companyName);
        List<SalesSummary> dailySalesSummaries = salesService.findSummaryByCompany(date,companyName);
        List<SalesSummary> productSummaries = salesService.findDetailsByCompany(date,companyName);
        List<BigDecimal> dailySales = new ArrayList<>();
        List<ProductMetricDTO> products = new ArrayList<>();

        MetricType metricType = MetricType.fromCode(type);

        // 根据指标类型提取不同的值
        dailySalesSummaries.forEach(salesSummary -> {
            dailySales.add(metricType == MetricType.VOLUME
                ? salesSummary.getTotalSales()
                : salesSummary.getTotalAmount());
        });

        productSummaries.forEach(product -> {
            ProductMetricDTO productMetricDTO = new ProductMetricDTO();
            productMetricDTO.setProductName(product.getProductName());
            productMetricDTO.setProductCode(product.getProductCode());
            productMetricDTO.setValue(metricType == MetricType.VOLUME
                ? product.getTotalSales()
                : product.getTotalAmount());
            productMetricDTO.setPercentage(metricType == MetricType.VOLUME
                ? product.getSalesRatio()
                : product.getAmountRatio());
            productMetricDTO.setRegion(product.getRegion());
            products.add(productMetricDTO);
        });

        List<CompanyDetailDTO> result = new ArrayList<>();
        CompanyDetailDTO companyDetailDTO = new CompanyDetailDTO();
        companyDetailDTO.setProducts(products);
        companyDetailDTO.setDailySales(dailySales);
        result.add(companyDetailDTO);
        return result;
    }
}
