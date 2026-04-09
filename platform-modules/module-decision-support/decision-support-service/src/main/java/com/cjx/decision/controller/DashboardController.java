package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;
import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.dashboard.SalesTrendProductDTO;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.entity.frorcl.CustomerTransactionDTO;
import com.cjx.decision.entity.frorcl.OrderDetail;
import com.cjx.decision.entity.frorcl.RawPriceDeviation;
import com.cjx.decision.service.CollectionService;
import com.cjx.decision.service.DashboardService;
import com.cjx.decision.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author cuijixu
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Validated
@Tag(name = "获取首页数据", description = "获取首页数据")
@CrossOrigin(origins = "*")
public class DashboardController {
    private final DashboardService dashboardService;
    private final OrderService orderService;
    private final CollectionService collectionService;


    @GetMapping("/metrics")
    public Result<DashboardMetricsDTO> getMetrics(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getMetrics(date));
    }

    @GetMapping("/orders")
    public Result<DashboardOrdersDTO> getOrders(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getOrders(date));
    }

    @GetMapping("/price-deviations")
    public Result<List<RawPriceDeviation>> getPriceDeviations(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dashboardService.getPriceDeviations(date));
    }

    @GetMapping("/price-deviations/details")
    public Result<List<CustomerTransactionDTO>> getDeviationDetails(@RequestParam String code,
                                                                    @RequestParam String region) {
        return Result.success(dashboardService.findCustomerTransaction(region,code));
    }

    @GetMapping("/sales-companies")
    public Result<List<CompanyMetricDTO>> getSalesDetail(
            @RequestParam(defaultValue = "amount") String type, // volume 或 amount
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dashboardService.getCompanyList(type,date));
    }

    @GetMapping("/sales-company-detail")
    public Result<List<CompanyDetailDTO>> getSalesCompanyDetail(
            @RequestParam String companyName,
            @RequestParam(defaultValue = "amount") String type,
            @RequestParam(defaultValue = "amount") String target,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dashboardService.getCompanyDetail(companyName, type, date,target));
    }

    @GetMapping("/order-company-detail")
    public Result<List<OrderDetail>> getOrderCompanyDetail(
            @RequestParam String companyName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(orderService.getCompanyDetails(date,companyName));
    }

    @GetMapping("/trends")
    public Result<List<SalesTrendProductDTO>> getSalesTrends(@RequestParam("date") String date) {
        return Result.success(dashboardService.getSalesTrends(date));
    }


    @GetMapping("/trends/year-detail")
    public Result<List<SalesTrendPointDTO>> getSalesTrendsList(@RequestParam("productCode") String productCode, @RequestParam("region") String region, @RequestParam("date") String date) {
        return Result.success(dashboardService.getSalesTrendsList(productCode,region,date));
    }

    @GetMapping("/product-deep-detail")
    public Result<ProductDeepDetail> getProductDeepDetail(@RequestParam("companyName") String companyName, @RequestParam("productCode") String productCode, @RequestParam("type") String type, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return  Result.success(dashboardService.getProductDeepDetail(companyName,productCode,type,date));
    }

    @GetMapping("/collection-companies")
    public Result<List<CompanyMetricDTO>> getCollectionDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(collectionService.getCollectionCompanies(date));
    }
}
