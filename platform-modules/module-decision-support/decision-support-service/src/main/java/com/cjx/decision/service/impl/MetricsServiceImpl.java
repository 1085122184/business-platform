package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;
import com.cjx.decision.dto.dashboard.RawCollection;
import com.cjx.decision.dto.dashboard.RawOrder;
import com.cjx.decision.dto.dashboard.RawSalesMetric;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.repository.frorcl.MetricsRepository;
import com.cjx.decision.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * 核心指标服务实现
 * 负责查询销量、销售额、回款、订单等核心指标
 *
 * @author system
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MetricsRepository metricsRepository;
    private final CaffeineCacheService caffeineCacheService;

    /**
     * 查询核心指标 (查询快)
     */
    @Override
    public DashboardMetricsDTO getMetrics(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yesterday = date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String monthStr = date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        SalesSummary salesSummary = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "summary:" + dateStr,
            k -> metricsRepository.findSummaryByDate(yesterday),
            SalesSummary.class
        );

        SalesSummary todaySum = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "todaySum:" + dateStr,
            k -> metricsRepository.findSummaryToToday(date),
            SalesSummary.class
        );

        SalesSummary countBudget = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "countBudget:" + dateStr,
            k -> metricsRepository.findCountBudget(monthStr),
            SalesSummary.class
        );

        SalesSummary amountBudget = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "amountBudget:" + dateStr,
            k -> metricsRepository.findAmountBudget(monthStr),
            SalesSummary.class
        );

        SalesSummary collectionMonth = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "collection:" + dateStr,
            k -> metricsRepository.findCollection(date.minusDays(1)),
            SalesSummary.class
        );
        
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        RawSalesMetric volume = new RawSalesMetric();
        BigDecimal totalSales = getValue(salesSummary, SalesSummary::getTotalSales);
        BigDecimal totalAmount = getValue(salesSummary, SalesSummary::getTotalAmount);
        BigDecimal monthSales = getValue(todaySum, SalesSummary::getTotalSales);
        BigDecimal monthAmount = getValue(todaySum, SalesSummary::getTotalAmount);
        BigDecimal countBudgetValue = getValue(countBudget, SalesSummary::getTotalCountBudget);
        BigDecimal amountBudgetValue = getValue(amountBudget, SalesSummary::getTotalAmountBudget);
        BigDecimal collectionAmount = getValue(collectionMonth, SalesSummary::getCollection);

        volume.setMetricName("总销量");
        volume.setDisplayValue(totalSales + " 吨");
        if (countBudgetValue != null && countBudgetValue.compareTo(BigDecimal.ZERO) != 0) {
            volume.setBudgetRate(monthSales.divide(countBudgetValue, 4, RoundingMode.HALF_UP));
        } else {
            volume.setBudgetRate(BigDecimal.ZERO);
        }
        volume.setGapValue(monthSales);//差额
        volume.setMonthGoal(countBudgetValue);//本月目标。月预算
        volume.setType("volume");
        dto.setSalesVolume(volume);

        RawSalesMetric amount = new RawSalesMetric();
        amount.setMetricName("总销售额");
        amount.setDisplayValue(totalAmount + " 万元");
        if (amountBudgetValue != null && amountBudgetValue.compareTo(BigDecimal.ZERO) != 0) {
            amount.setBudgetRate(monthAmount.divide(amountBudgetValue, 4, RoundingMode.HALF_UP));
        } else {
            amount.setBudgetRate(BigDecimal.ZERO);
        }
        amount.setGapValue(monthAmount);
        amount.setMonthGoal(amountBudgetValue);
        amount.setType("amount");
        dto.setSalesAmount(amount);
        RawCollection collection = new RawCollection();
        if(Objects.nonNull(collectionMonth)){
            collection.setCollectionAmount(collectionAmount + " 万元");
            if (monthAmount.compareTo(BigDecimal.ZERO) != 0) {
                collection.setCollectionRate(collectionAmount.divide(monthAmount, 4, RoundingMode.HALF_UP));
            } else {
                collection.setCollectionRate(BigDecimal.ZERO);
            }
            collection.setGapValue(monthAmount.subtract(collectionAmount));
            collection.setMonthGoal(monthAmount);
        }
        dto.setCollection(collection);
        return dto;
    }

    @Override
    public DashboardOrdersDTO getOrders(LocalDate targetDate) {
        DashboardOrdersDTO dto = new DashboardOrdersDTO();

        String thisMonth = targetDate.minusDays(1).withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String lastMonth = targetDate.minusDays(1).with(TemporalAdjusters.firstDayOfNextMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        SalesSummary monthOrder = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "monthOrder:" + dateStr,
            k -> metricsRepository.findMonthOrder(thisMonth, lastMonth, targetDate),
            SalesSummary.class
        );
        
        RawOrder monthOrders = new RawOrder();
        monthOrders.setOrderTitle("本月未关订单数");
        BigDecimal monthOpenOrder = getValue(monthOrder, SalesSummary::getOpenOrder);
        BigDecimal monthTotalOrder = getValue(monthOrder, SalesSummary::getTotalOrder);
        monthOrders.setOrderCount(monthOpenOrder + "");
        monthOrders.setOrderRate(calculateRate(monthOpenOrder, monthTotalOrder));
        monthOrders.setBarColor("#f59e0b");
        dto.setMonthOrders(monthOrders);

        String thisYear = targetDate.minusDays(1).withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy"));
        String yesterday = targetDate.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        SalesSummary yearOrder = caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, "yearOrder:" + dateStr,
            k -> metricsRepository.findYearOrder(thisYear, yesterday),
            SalesSummary.class
        );
        
        RawOrder yearOrders = new RawOrder();
        yearOrders.setOrderTitle("本年未关订单数");
        BigDecimal yearOpenOrder = getValue(yearOrder, SalesSummary::getOpenOrder);
        BigDecimal yearTotalOrder = getValue(yearOrder, SalesSummary::getTotalOrder);
        yearOrders.setOrderCount(yearOpenOrder + "");
        yearOrders.setOrderRate(calculateRate(yearOpenOrder, yearTotalOrder));
        yearOrders.setBarColor("#f59e0b");
        dto.setYearOrders(yearOrders);

        return dto;
    }

    private BigDecimal getValue(SalesSummary summary, java.util.function.Function<SalesSummary, BigDecimal> getter) {
        if (summary == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = getter.apply(summary);
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calculateRate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeNumerator = numerator == null ? BigDecimal.ZERO : numerator;
        return safeNumerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
