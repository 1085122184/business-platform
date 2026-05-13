package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.dashboard.OrderDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.projection.frorcl.OrderDetail;
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
 * 销售分析Controller
 * 提供销售明细和公司详情查询接口
 * 
 * @author system
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/sales-analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "销售分析", description = "查询销售明细和公司详情数据")
@CrossOrigin(origins = "*")
public class SalesAnalysisController {

    private final DashboardService dashboardService;
    
    /**
     * 查询销售公司列表
     * @param type 指标类型(volume=销量, amount=销售额),默认amount
     * @param date 日期
     * @return 公司指标列表
     */
    @GetMapping("/companies")
    public Result<List<CompanyMetricDTO>> getCompanyList(
            @RequestParam(defaultValue = "amount") String type,
            @NotNull(message = "日期不能为空") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dashboardService.getCompanyList(type, date));
    }
    
    /**
     * 查询销售公司详情
     * @param companyName 公司名称
     * @param type 指标类型(volume=销量, amount=销售额),默认amount
     * @param date 日期
     * @return 公司详情
     */
    @GetMapping("/companies/detail")
    public Result<List<CompanyDetailDTO>> getCompanyDetail(
            @NotBlank(message = "公司名称不能为空") @RequestParam String companyName,
            @RequestParam(defaultValue = "amount") String type,
            @NotNull(message = "日期不能为空") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dashboardService.getCompanyDetail(companyName, type, date));
    }
    
    /**
     * 查询订单公司详情
     * @param companyName 公司名称
     * @param date 日期
     * @return 订单详情
     */
    @GetMapping("/orders/company-detail")
    public Result<List<OrderDetailDTO>> getOrderCompanyDetail(
            @NotBlank(message = "公司名称不能为空") @RequestParam String companyName,
            @NotNull(message = "日期不能为空") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dashboardService.getOrderCompanyDetails(companyName, date));
    }

    /**
     * 查询产品深度详情
     * @param companyName 公司名称
     * @param productCode 产品代码
     * @param type 分析类型(month=月度, year=年度)
     * @param date 日期
     * @return 产品深度详情
     */
    @GetMapping("/product-deep")
    public Result<ProductDeepDetail> getProductDeepDetail(
            @NotBlank(message = "公司名称不能为空") @RequestParam("companyName") String companyName,
            @NotBlank(message = "产品代码不能为空") @RequestParam("productCode") String productCode,
            @NotBlank(message = "分析类型不能为空") @RequestParam("type") String type,
            @NotNull(message = "日期不能为空") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getProductDeepDetail(companyName, productCode, type, date));
    }
}
