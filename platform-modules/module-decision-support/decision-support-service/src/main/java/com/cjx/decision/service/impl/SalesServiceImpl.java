package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.annotation.AutoWarmUp;
import com.cjx.decision.constant.CompanyCodeConstant;
import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.enums.MetricType;
import com.cjx.decision.enums.RegionCode;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.repository.frorcl.SalesRepository;
import com.cjx.decision.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cuijixu
 */
@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {
    private final SalesRepository salesRepository;
    private final CaffeineCacheService caffeineCacheService;

    @AutoWarmUp
    @Override
    public SalesSummary findSummaryByDate(LocalDate date) {
//        String yesterday = LocalDate.now().minusDays(1)
//                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yesterday = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "todaySalesList:" + date;
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findSummaryByDate(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findSummaryToToday(LocalDate date) {
        String key = "totalSalesList:" + date;
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findSummaryToToday(date),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findCountBudget(LocalDate date) {
        String yesterday = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String key = "countBudget:" + date;
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findCountBudget(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findAmountBudget(LocalDate date) {
        String yesterday = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String key = "amountBudget:" + date;
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findAmountBudget(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findMonthOrder(LocalDate targetDate) {
        String thisMonth = targetDate.minusDays(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String lastMonth = targetDate.minusDays(1).with(TemporalAdjusters.lastDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "monthOrder:" + targetDate;
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findMonthOrder(thisMonth,lastMonth,targetDate),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findYearOrder(LocalDate targetDate) {
        String thisYear = targetDate.minusDays(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy"));
        String key = "yearOrder:" + targetDate;
        String yesterday = targetDate.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findYearOrder(thisYear,yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findCollection(LocalDate date) {
        LocalDate yesterday = date.minusDays(1);
        String key = "collection:" + date;
        return caffeineCacheService.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findCollection(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public List<RawPriceDeviation> findPriceDiff(LocalDate date) {
        String startDate = date.minusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "priceDiff:" + date;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findPriceDiff(startDate,endDate));
    }

    @Override
    public List<CustomerTransactionProjection> findCustomerTransaction(String region, String code, String targetDate) {
        // 使用RegionCode枚举将区域名称转换为代码
        RegionCode regionCode = RegionCode.fromCode(region);
        String finalRegion = regionCode.getCode();
        String key = "customerTransaction:" + targetDate;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findCustomerTransaction(finalRegion, code, targetDate));
    }


    @Override
    public List<CompanyMetricDTO> findSaleDetails(String type, LocalDate targetDate) {
        String key = "saleDetails:"+ type + ":" + targetDate;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> this.getCompanyMetricDTOS(type,targetDate));
    }

    @NotNull
    private List<CompanyMetricDTO> getCompanyMetricDTOS(String type, LocalDate targetDate) {
        BigDecimal multiplier = new BigDecimal("1");
        String yesterday = targetDate.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<SalesSummary> volumeBudget = salesRepository.findCountBudgetDetails(yesterday);
        List<SalesSummary> amountBudget = salesRepository.findAmountBudgetDetails(yesterday);
        List<SalesSummary> salesAll = salesRepository.findSummaryTodayDetails(targetDate);
        Map<String, SalesSummary> volumeMap = volumeBudget.stream()
                .collect(Collectors.toMap(SalesSummary::getCompanyName, b -> b, (a, b) -> a));
        Map<String, SalesSummary> amountMap = amountBudget.stream()
                .collect(Collectors.toMap(SalesSummary::getCompanyName, b -> b, (a, b) -> a));

        MetricType metricType = MetricType.fromCode(type);

        return salesAll.stream()
                .map(s -> {
                    BigDecimal actualValue = new BigDecimal(1);
                    BigDecimal targetValue = new BigDecimal(1);
                    SalesSummary b = null;
                    if (metricType == MetricType.VOLUME) {
                        b = volumeMap.get(s.getCompanyCode());
                        if (b != null) {
                            actualValue = s.getTotalSales().multiply(multiplier);
                            targetValue = b.getTotalCountBudget().multiply(multiplier);
                        }
                    } else {
                        b = amountMap.get(s.getCompanyCode());
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
    }

    @Override
    public List<SalesSummary> findSummaryByCompany(LocalDate targetDate, String company) {
        String key = "companySummary:" + company + ":" + targetDate;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findSummaryByCompany(targetDate,company));
    }

    @Override
    public List<SalesSummary> findDetailsByCompany(LocalDate targetDate, String company) {
        String key = "companyDetails:" + company + ":" + targetDate;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findDetailsByCompany(targetDate,company));
    }

    @Override
    public List<SalesSummary> findTrendsToday(String targetDate) {
        String key = "trendsToday:" + targetDate;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findTrendsToday(targetDate));
    }

    @AutoWarmUp
    @Override
    public List<SalesSummary> findTrendsAll(LocalDate endLocalDate) {
        String startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "trendsAll:" + endDate;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findTrendsAll(startDate,endDate));
    }

    @Override
    public List<SalesSummary> findTrendsYear(LocalDate endLocalDate,String productCode,String region) {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(now.getYear(), 1, 1);
        String startDate = firstDayOfYear.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "trendsYear:" + endDate + ":" + productCode+"_"+region;
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findTrendsYear(startDate,endDate,productCode,region));
    }

    @Override
    public List<SalesSummary> getProductDeepMonth(String companyName, String productCode,LocalDate date) {
        companyName = CompanyCodeConstant.COMPANY_CODE_MAP.getOrDefault(companyName, companyName);
        return salesRepository.getProductDeepMonth(companyName,productCode,date);
    }

    @Override
    public List<SalesSummary> getProductDeepYear(String companyName, String productCode,LocalDate date) {
        companyName = CompanyCodeConstant.COMPANY_CODE_MAP.getOrDefault(companyName, companyName);
        return salesRepository.getProductDeepYear(companyName,productCode,date);
    }

    @Override
    public List<SalesSummary> getProductCustomer(String companyName, String productCode,LocalDate date) {
        companyName = CompanyCodeConstant.COMPANY_CODE_MAP.getOrDefault(companyName, companyName);
        return salesRepository.getProductCustomer(companyName,productCode,date);
    }


    @AutoWarmUp
    public void warmUpComplexData(LocalDate targetDate) {
        this.findSaleDetails(MetricType.VOLUME.getCode(), targetDate);
        this.findSaleDetails(MetricType.AMOUNT.getCode(), targetDate);
        List<String> companies = Arrays.asList("3000", "1400", "1300", "1200");
        for (String company : companies) {
            this.findSummaryByCompany(targetDate, company);
            this.findDetailsByCompany(targetDate, company);
        }

    }
}
