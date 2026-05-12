package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.dashboard.SalesTrendProductDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 趋势分析Controller
 * 提供销售趋势和产品深度查询接口
 * 
 * @author system
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/trend-analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "趋势分析", description = "查询销售趋势和产品深度数据")
@CrossOrigin(origins = "*")
public class TrendAnalysisController {
    
    private final DashboardService dashboardService;
    
    /**
     * 查询月度销售趋势
     * @param date 日期
     * @return 销售趋势列表
     */
    @GetMapping("/monthly")
    public Result<List<SalesTrendProductDTO>> getMonthlyTrends(
            @NotNull(message = "日期不能为空") @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getSalesTrends(date));
    }
    
    /**
     * 查询年度销售趋势详情
     * @param productCode 产品代码
     * @param region 区域
     * @param date 日期
     * @return 销售趋势点列表
     */
    @GetMapping("/yearly")
    public Result<List<SalesTrendPointDTO>> getYearlyTrends(
            @NotBlank(message = "产品代码不能为空") @RequestParam("productCode") String productCode,
            @NotBlank(message = "区域不能为空") @RequestParam("region") String region,
            @NotNull(message = "日期不能为空") @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getSalesTrendsList(productCode, region, date));
    }
    

}
