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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final MetricsRepository metricsRepository;
    private final CaffeineCacheService caffeineCacheService;

    @Override
    public DashboardMetricsDTO getMetrics(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        String yesterday = date.minusDays(1).format(DATE_FORMATTER);
        String monthStr = date.minusDays(1).format(MONTH_FORMATTER);

        SalesSummary salesSummary = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "summary:" + dateStr,
                k -> metricsRepository.findSummaryByDate(yesterday),
                SalesSummary.class
        );

        SalesSummary todaySum = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "todaySum:" + dateStr,
                k -> metricsRepository.findSummaryToToday(date),
                SalesSummary.class
        );

        SalesSummary countBudget = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "countBudget:" + dateStr,
                k -> metricsRepository.findCountBudget(monthStr),
                SalesSummary.class
        );

        SalesSummary amountBudget = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "amountBudget:" + dateStr,
                k -> metricsRepository.findAmountBudget(monthStr),
                SalesSummary.class
        );

        SalesSummary collectionMonth = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "collection:" + dateStr,
                k -> metricsRepository.findCollection(date.minusDays(1)),
                SalesSummary.class
        );

        BigDecimal totalSales = getValue(salesSummary, SalesSummary::getTotalSales);
        BigDecimal totalAmount = getValue(salesSummary, SalesSummary::getTotalAmount);
        BigDecimal monthSales = getValue(todaySum, SalesSummary::getTotalSales);
        BigDecimal monthAmount = getValue(todaySum, SalesSummary::getTotalAmount);
        BigDecimal countBudgetValue = getValue(countBudget, SalesSummary::getTotalCountBudget);
        BigDecimal amountBudgetValue = getValue(amountBudget, SalesSummary::getTotalAmountBudget);
        BigDecimal collectionAmount = getValue(collectionMonth, SalesSummary::getCollection);

        DashboardMetricsDTO dto = new DashboardMetricsDTO();

        RawSalesMetric volume = new RawSalesMetric();
        volume.setMetricName("总销量");
        volume.setDisplayValue(totalSales + " 吨");
        volume.setBudgetRate(calculateRate(monthSales, countBudgetValue, 4));
        volume.setGapValue(monthSales);
        volume.setMonthGoal(countBudgetValue);
        volume.setType("volume");
        dto.setSalesVolume(volume);

        RawSalesMetric amount = new RawSalesMetric();
        amount.setMetricName("总销售额");
        amount.setDisplayValue(totalAmount + " 万元");
        amount.setBudgetRate(calculateRate(monthAmount, amountBudgetValue, 4));
        amount.setGapValue(monthAmount);
        amount.setMonthGoal(amountBudgetValue);
        amount.setType("amount");
        dto.setSalesAmount(amount);

        RawCollection collection = new RawCollection();
        collection.setCollectionAmount(collectionAmount + " 万元");
        collection.setCollectionRate(calculateRate(collectionAmount, monthAmount, 4));
        collection.setGapValue(monthAmount.subtract(collectionAmount));
        collection.setMonthGoal(monthAmount);
        dto.setCollection(collection);

        return dto;
    }

    @Override
    public DashboardOrdersDTO getOrders(LocalDate targetDate) {
        DashboardOrdersDTO dto = new DashboardOrdersDTO();

        String thisMonth = targetDate.minusDays(1).withDayOfMonth(1).format(DATE_FORMATTER);
        String lastMonth = targetDate.minusDays(1).with(TemporalAdjusters.firstDayOfNextMonth()).format(DATE_FORMATTER);
        String dateStr = targetDate.format(DATE_FORMATTER);

        SalesSummary monthOrder = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "monthOrder:" + dateStr,
                k -> metricsRepository.findMonthOrder(thisMonth, lastMonth, targetDate),
                SalesSummary.class
        );

        RawOrder monthOrders = new RawOrder();
        BigDecimal monthOpenOrder = getValue(monthOrder, SalesSummary::getOpenOrder);
        BigDecimal monthTotalOrder = getValue(monthOrder, SalesSummary::getTotalOrder);
        monthOrders.setOrderTitle("本月未关订单数");
        monthOrders.setOrderCount(monthOpenOrder + "");
        monthOrders.setOrderRate(calculateRate(monthOpenOrder, monthTotalOrder, 2));
        monthOrders.setBarColor("#f59e0b");
        dto.setMonthOrders(monthOrders);

        String thisYear = targetDate.minusDays(1).withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy"));
        String yesterday = targetDate.minusDays(1).format(DATE_FORMATTER);

        SalesSummary yearOrder = caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                "yearOrder:" + dateStr,
                k -> metricsRepository.findYearOrder(thisYear, yesterday),
                SalesSummary.class
        );

        RawOrder yearOrders = new RawOrder();
        BigDecimal yearOpenOrder = getValue(yearOrder, SalesSummary::getOpenOrder);
        BigDecimal yearTotalOrder = getValue(yearOrder, SalesSummary::getTotalOrder);
        yearOrders.setOrderTitle("本年未关订单数");
        yearOrders.setOrderCount(yearOpenOrder + "");
        yearOrders.setOrderRate(calculateRate(yearOpenOrder, yearTotalOrder, 2));
        yearOrders.setBarColor("#f59e0b");
        dto.setYearOrders(yearOrders);

        return dto;
    }

    private BigDecimal getValue(SalesSummary summary, Function<SalesSummary, BigDecimal> getter) {
        if (summary == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = getter.apply(summary);
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calculateRate(BigDecimal numerator, BigDecimal denominator, int scale) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeNumerator = numerator == null ? BigDecimal.ZERO : numerator;
        return safeNumerator.divide(denominator, scale, RoundingMode.HALF_UP);
    }
}
