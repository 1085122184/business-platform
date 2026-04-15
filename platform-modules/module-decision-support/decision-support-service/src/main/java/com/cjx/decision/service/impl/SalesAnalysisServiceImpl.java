package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.constant.CompanyCodeConstant;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductMetricDTO;
import com.cjx.decision.enums.MetricType;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.repository.frorcl.SalesAnalysisRepository;
import com.cjx.decision.service.SalesAnalysisService;
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
 * 销售分析服务实现
 * 负责查询销售明细和公司详情数据
 *
 * @author system
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SalesAnalysisServiceImpl implements SalesAnalysisService {

    private final SalesAnalysisRepository salesAnalysisRepository;
    private final CaffeineCacheService caffeineCacheService;

    /**
     * 获取销售明细看板数据 (提供给前端直接使用)
     */
    @Override
    public List<CompanyMetricDTO> getCompanyList(String type, LocalDate date) {
        String key = "saleDetails:" + type + ":" + date;
        return caffeineCacheService.getOrLoadList(
            CacheType.TODAY_DATA, key,
            k -> findSaleDetails(type, date)
        );
    }

    private List<CompanyMetricDTO> findSaleDetails(String type, LocalDate targetDate) {
        BigDecimal multiplier = new BigDecimal("1");
        String yesterday = targetDate.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<SalesSummary> volumeBudget = salesAnalysisRepository.findCountBudgetDetails(yesterday);
        List<SalesSummary> amountBudget = salesAnalysisRepository.findAmountBudgetDetails(yesterday);
        List<SalesSummary> salesAll = salesAnalysisRepository.findSummaryTodayDetails(targetDate);
        Map<String, SalesSummary> volumeMap = volumeBudget.stream()
                .collect(Collectors.toMap(SalesSummary::getCompanyName, b -> b, (a, b) -> a));
        Map<String, SalesSummary> amountMap = amountBudget.stream()
                .collect(Collectors.toMap(SalesSummary::getCompanyName, b -> b, (a, b) -> a));

        MetricType metricType = MetricType.fromCode(type);

        List<CompanyMetricDTO> result = salesAll.stream()
                .map(s -> {
                    BigDecimal actualValue = new BigDecimal(1);
                    BigDecimal targetValue = new BigDecimal(1);
                    SalesSummary b = null;
                    if (metricType == MetricType.VOLUME) {
                        b = volumeMap.get(s.getCompanyName());
                        if (b != null) {
                            actualValue = s.getTotalSales().multiply(multiplier);
                            targetValue = b.getTotalCountBudget().multiply(multiplier);
                        }
                    } else {
                        b = amountMap.get(s.getCompanyName());
                        if (b != null) {
                            actualValue = s.getTotalAmount().multiply(multiplier);
                            targetValue = b.getTotalAmountBudget().multiply(multiplier);
                        }
                    }

                    CompanyMetricDTO item = new CompanyMetricDTO();
                    item.setCompanyName(s.getCompanyName());
                    item.setValue(actualValue);
                    item.setTarget(targetValue);
                    return item;
                })
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<CompanyDetailDTO> getCompanyDetail(String companyName, String type, LocalDate date) {
        String key = "companyDetail:" + companyName + ":" + type + ":" + date;
        return caffeineCacheService.getOrLoadList(
            CacheType.TODAY_DATA, key,
            k -> buildCompanyDetail(companyName, type, date)
        );
    }

    private List<CompanyDetailDTO> buildCompanyDetail(String companyName, String type, LocalDate date) {
        companyName = CompanyCodeConstant.COMPANY_CODE_MAP.getOrDefault(companyName, companyName);
        List<SalesSummary> dailySalesSummaries = salesAnalysisRepository.findSummaryByCompany(date, companyName);
        List<SalesSummary> productSummaries = salesAnalysisRepository.findDetailsByCompany(date, companyName);
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
