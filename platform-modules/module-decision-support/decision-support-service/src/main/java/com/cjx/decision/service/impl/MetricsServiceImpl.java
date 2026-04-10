package com.cjx.decision.service.impl;

import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;
import com.cjx.decision.dto.dashboard.RawCollection;
import com.cjx.decision.dto.dashboard.RawOrder;
import com.cjx.decision.dto.dashboard.RawSalesMetric;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.service.MetricsService;
import com.cjx.decision.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    
    private final SalesService salesService;

    /**
     * 查询核心指标 (查询快)
     */
    @Override
    public DashboardMetricsDTO getMetrics(LocalDate date) {
        SalesSummary salesSummary = salesService.findSummaryByDate(date);//昨日
        SalesSummary todaySum = salesService.findSummaryToToday(date); //累计
        SalesSummary countBudget = salesService.findCountBudget(date); //销量预算
        SalesSummary amountBudget = salesService.findAmountBudget(date); //销售额预算
        SalesSummary collectionMonth = salesService.findCollection(date);//本月回款
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        RawSalesMetric volume = new RawSalesMetric();
        volume.setMetricName("总销量");
        volume.setDisplayValue(salesSummary.getTotalSales()+" 吨");
        BigDecimal countBudgetValue = countBudget.getTotalCountBudget();
        if (countBudgetValue != null && countBudgetValue.compareTo(BigDecimal.ZERO) != 0) {
            volume.setBudgetRate(todaySum.getTotalSales().divide(countBudgetValue, 4, RoundingMode.HALF_UP));
        } else {
            volume.setBudgetRate(BigDecimal.ZERO);
        }
        volume.setGapValue(todaySum.getTotalSales());//差额
        volume.setMonthGoal(countBudget.getTotalCountBudget());//本月目标。月预算
        volume.setType("volume");
        dto.setSalesVolume(volume);

        RawSalesMetric amount = new RawSalesMetric();
        amount.setMetricName("总销售额");
        amount.setDisplayValue(salesSummary.getTotalAmount()+" 万元");
        BigDecimal amountBudgetValue = amountBudget.getTotalAmountBudget();
        if (amountBudgetValue != null && amountBudgetValue.compareTo(BigDecimal.ZERO) != 0) {
            amount.setBudgetRate(todaySum.getTotalAmount().divide(amountBudgetValue, 4, RoundingMode.HALF_UP));
        } else {
            amount.setBudgetRate(BigDecimal.ZERO);
        }
        amount.setGapValue(todaySum.getTotalAmount());
        amount.setMonthGoal(amountBudget.getTotalAmountBudget());
        amount.setType("amount");
        dto.setSalesAmount(amount);
        RawCollection collection = new RawCollection();
        if(Objects.nonNull(collectionMonth)){
            collection.setCollectionAmount(collectionMonth.getCollection()+" 万元");
            BigDecimal totalAmount = todaySum.getTotalAmount();
            if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) != 0) {
                collection.setCollectionRate(collectionMonth.getCollection().divide(totalAmount, 4, RoundingMode.HALF_UP));
            } else {
                collection.setCollectionRate(BigDecimal.ZERO);
            }
            collection.setGapValue(todaySum.getTotalAmount().subtract(collectionMonth.getCollection()));
            collection.setMonthGoal(todaySum.getTotalAmount());
        }
        dto.setCollection(collection);
        return dto;
    }

    @Override
    public DashboardOrdersDTO getOrders(LocalDate targetDate) {
        DashboardOrdersDTO dto = new DashboardOrdersDTO();

        SalesSummary monthOrder = salesService.findMonthOrder(targetDate);
        RawOrder monthOrders = new RawOrder();
        monthOrders.setOrderTitle("本月未关订单数");
        monthOrders.setOrderCount(monthOrder.getOpenOrder()+"");
        monthOrders.setOrderRate(monthOrder.getOpenOrder().divide(monthOrder.getTotalOrder(), 2, RoundingMode.HALF_UP));
        monthOrders.setBarColor("#f59e0b");
        dto.setMonthOrders(monthOrders);

        SalesSummary yearOrder = salesService.findYearOrder(targetDate);
        RawOrder yearOrders = new RawOrder();
        yearOrders.setOrderTitle("本年未关订单数");
        yearOrders.setOrderCount(yearOrder.getOpenOrder()+"");
        yearOrders.setOrderRate(yearOrder.getOpenOrder().divide(yearOrder.getTotalOrder(), 2, RoundingMode.HALF_UP));
        yearOrders.setBarColor("#f59e0b");
        dto.setYearOrders(yearOrders);

        return dto;
    }
}
