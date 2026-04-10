package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineUtil;
import com.cjx.decision.annotation.AutoWarmUp;
import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
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
    private final CaffeineUtil caffeineUtil;

    @AutoWarmUp
    @Override
    public SalesSummary findSummaryByDate(LocalDate date) {
//        String yesterday = LocalDate.now().minusDays(1)
//                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yesterday = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "todaySalesList:" + date;
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findSummaryByDate(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findSummaryToToday(LocalDate date) {
        String key = "totalSalesList:" + date;
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findSummaryToToday(date),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findCountBudget(LocalDate date) {
        String yesterday = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String key = "countBudget:" + date;
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findCountBudget(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findAmountBudget(LocalDate date) {
        String yesterday = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String key = "amountBudget:" + date;
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findAmountBudget(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findMonthOrder(LocalDate targetDate) {
        String thisMonth = targetDate.minusDays(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String lastMonth = targetDate.minusDays(1).with(TemporalAdjusters.lastDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "monthOrder:" + targetDate;
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findMonthOrder(thisMonth,lastMonth,targetDate),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findYearOrder(LocalDate targetDate) {
        String thisYear = targetDate.minusDays(1).withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy"));
        String key = "yearOrder:" + targetDate;
        String yesterday = targetDate.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findYearOrder(thisYear,yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public SalesSummary findCollection(LocalDate date) {
        LocalDate yesterday = date.minusDays(1);
        String key = "collection:" + date;
        return caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key, k -> salesRepository.findCollection(yesterday),SalesSummary.class);
    }

    @AutoWarmUp
    @Override
    public List<RawPriceDeviation> findPriceDiff(LocalDate date) {
        String startDate = date.minusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = date.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "priceDiff:" + date;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findPriceDiff(startDate,endDate));
    }

    @Override
    public List<CustomerTransactionProjection> findCustomerTransaction(String region, String code, String targetDate) {
        if("国内".equals(region)){
            region = "10";
        }else {
            region = "20";
        }
        String key = "customerTransaction:" + targetDate;
        String finalRegion = region;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findCustomerTransaction(finalRegion,code,targetDate));
    }


    @Override
    public List<CompanyMetricDTO> findSaleDetails(String type, LocalDate targetDate) {
        String key = "saleDetails:"+ type + ":" + targetDate;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> this.getCompanyMetricDTOS(type,targetDate));
    }

    @NotNull
    private List<CompanyMetricDTO> getCompanyMetricDTOS(String type, LocalDate targetDate) {
        List<CompanyMetricDTO> result = new ArrayList<>();
        BigDecimal multiplier = new BigDecimal("1");
        String yesterday = targetDate.minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<SalesSummary> volumeBudget = salesRepository.findCountBudgetDetails(yesterday);
        List<SalesSummary> amountBudget = salesRepository.findAmountBudgetDetails(yesterday);
        List<SalesSummary> salesAll = salesRepository.findSummaryTodayDetails(targetDate);
        Map<String, SalesSummary> volumeMap = volumeBudget.stream()
                .collect(Collectors.toMap(SalesSummary::getCompanyName, b -> b, (a, b) -> a));
        Map<String, SalesSummary> amountMap = amountBudget.stream()
                .collect(Collectors.toMap(SalesSummary::getCompanyName, b -> b, (a, b) -> a));
        result = salesAll.stream()
                .map(s -> {
                    BigDecimal actualValue = new BigDecimal(1);
                    BigDecimal targetValue = new BigDecimal(1);
                    SalesSummary b = null;
                    if("volume".equals(type)){
                        b = volumeMap.get(s.getCompanyName());
                        if (b != null) {
                            actualValue = BigDecimal.valueOf(s.getTotalSales()).multiply(multiplier);
                            targetValue = BigDecimal.valueOf(b.getTotalCountBudget()).multiply(multiplier);
                        }
                    }else if("amount".equals(type)){
                        b = amountMap.get(s.getCompanyName());
                        if (b != null) {
                            actualValue = BigDecimal.valueOf(s.getTotalAmount()).multiply(multiplier);
                            targetValue = BigDecimal.valueOf(b.getTotalAmountBudget()).multiply(multiplier);
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
    public List<SalesSummary> findSummaryByCompany(LocalDate targetDate, String company) {
        String key = "companySummary:" + company + ":" + targetDate;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findSummaryByCompany(targetDate,company));
    }

    @Override
    public List<SalesSummary> findDetailsByCompany(LocalDate targetDate, String company) {
        String key = "companyDetails:" + company + ":" + targetDate;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findDetailsByCompany(targetDate,company));
    }

    @Override
    public List<SalesSummary> findTrendsToday(String targetDate) {
        String key = "trendsToday:" + targetDate;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findTrendsToday(targetDate));
    }

    @AutoWarmUp
    @Override
    public List<SalesSummary> findTrendsAll(LocalDate endLocalDate) {
        String startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "trendsAll:" + endDate;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findTrendsAll(startDate,endDate));
    }

    @Override
    public List<SalesSummary> findTrendsYear(LocalDate endLocalDate,String productCode,String region) {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(now.getYear(), 1, 1);
        String startDate = firstDayOfYear.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "trendsYear:" + endDate + ":" + productCode+"_"+region;
        return caffeineUtil.getOrLoadList(CacheType.TODAY_DATA,key, k -> salesRepository.findTrendsYear(startDate,endDate,productCode,region));
    }

    @Override
    public List<SalesSummary> getProductDeepMonth(String companyName, String productCode,LocalDate date) {
        if ("绿冷".equals(companyName)){
            companyName = "3001";
        }else if ("有机硅".equals(companyName)){
            companyName = "1400";
        }else if ("氟硅".equals(companyName)){
            companyName = "1301";
        }else if ("高分子".equals(companyName)){
            companyName = "1201";
        }
        return salesRepository.getProductDeepMonth(companyName,productCode,date);
    }

    @Override
    public List<SalesSummary> getProductDeepYear(String companyName, String productCode,LocalDate date) {
        if ("绿冷".equals(companyName)){
            companyName = "3000";
        }else if ("有机硅".equals(companyName)){
            companyName = "1400";
        }else if ("氟硅".equals(companyName)){
            companyName = "1300";
        }else if ("高分子".equals(companyName)){
            companyName = "1200";
        }
        return salesRepository.getProductDeepYear(companyName,productCode,date);
    }

    @Override
    public List<SalesSummary> getProductCustomer(String companyName, String productCode,LocalDate date) {
        if ("绿冷".equals(companyName)){
            companyName = "3000";
        }else if ("有机硅".equals(companyName)){
            companyName = "1400";
        }else if ("氟硅".equals(companyName)){
            companyName = "1300";
        }else if ("高分子".equals(companyName)){
            companyName = "1200";
        }
        return salesRepository.getProductCustomer(companyName,productCode,date);
    }


    @AutoWarmUp
    public void warmUpComplexData(LocalDate targetDate) {
        this.findSaleDetails("volume", targetDate);
        this.findSaleDetails("amount", targetDate);
        List<String> companies = Arrays.asList("3001", "1400", "1301", "1201");
        for (String company : companies) {
            this.findSummaryByCompany(targetDate, company);
            this.findDetailsByCompany(targetDate, company);
        }

    }
}
