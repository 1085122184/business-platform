package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.constant.DecisionPermissions;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 价格分析Controller
 * 提供价格偏差和客户交易查询接口
 * 
 * @author system
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/price-analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "价格分析", description = "查询价格偏差和客户交易数据")
@CrossOrigin(origins = "*")
@PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRICE_ANALYSIS_VIEW)")
public class PriceAnalysisController {
    
    private final DashboardService dashboardService;
    
    /**
     * 查询价格偏差列表
     * @param date 日期
     * @return 价格偏差列表
     */
    @GetMapping("/deviations")
    public Result<List<RawPriceDeviation>> getPriceDeviations(
            @NotNull(message = "日期不能为空") @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getPriceDeviations(date));
    }
    
    /**
     * 查询客户交易详情
     * @param code 产品代码
     * @param region 区域(国内/国外)
     * @param date 日期
     * @return 客户交易列表
     */
    @GetMapping("/deviations/details")
    public Result<List<CustomerTransactionProjection>> getDeviationDetails(
            @NotBlank(message = "产品代码不能为空") @RequestParam("code") String code,
            @NotBlank(message = "区域不能为空") @RequestParam("region") String region,
            @NotBlank(message = "类型不能为空") @RequestParam("type") String type,
            @NotNull(message = "日期不能为空") @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.findCustomerTransaction(region, code, type, date));
    }
}
