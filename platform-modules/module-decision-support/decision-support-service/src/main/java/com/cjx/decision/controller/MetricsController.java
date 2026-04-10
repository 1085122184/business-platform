package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;
import com.cjx.decision.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
public class MetricsController {
    
    private final DashboardService dashboardService;
    
    /**
     * 查询核心指标(销量/销售额/回款)
     * @param date 日期,不传默认查询昨天
     * @return 核心指标
     */
    @GetMapping
    public Result<DashboardMetricsDTO> getMetrics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getMetrics(date));
    }
    
    /**
     * 查询订单数据(本月/本年未关单)
     * @param date 日期,不传默认查询昨天
     * @return 订单数据
     */
    @GetMapping("/orders")
    public Result<DashboardOrdersDTO> getOrders(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getOrders(date));
    }
}
