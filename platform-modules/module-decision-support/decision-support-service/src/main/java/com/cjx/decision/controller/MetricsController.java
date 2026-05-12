package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.constant.DecisionPermissions;
import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;
import com.cjx.decision.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 核心指标Controller
 * 提供销量、销售额、回款、订单等核心指标查询接口
 * 
 * @author system
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Validated
@Tag(name = "核心指标", description = "查询销量、销售额、回款、订单等核心指标")
@CrossOrigin(origins = "*")
@PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).METRICS_VIEW)")
public class MetricsController {
    
    private final DashboardService dashboardService;
    
    /**
     * 查询核心指标(销量/销售额/回款)
     * @param date 日期
     * @return 核心指标
     */
    @GetMapping
    public Result<DashboardMetricsDTO> getMetrics(
            @NotNull(message = "日期不能为空") @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getMetrics(date));
    }
    
    /**
     * 查询订单数据(本月/本年未关单)
     * @param date 日期
     * @return 订单数据
     */
    @GetMapping("/orders")
    public Result<DashboardOrdersDTO> getOrders(
            @NotNull(message = "日期不能为空") @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getOrders(date));
    }
}
