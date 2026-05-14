package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.production.ProductionDetailPageDTO;
import com.cjx.decision.dto.production.ProductionOverviewDTO;
import com.cjx.decision.dto.production.ProductionOutputDetailDTO;
import com.cjx.decision.dto.production.ProductionRankItemDTO;
import com.cjx.decision.dto.production.ProductionRawConsumptionDetailDTO;
import com.cjx.decision.dto.production.ProductionTransportDetailDTO;
import com.cjx.decision.service.ProductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@Validated
@Tag(name = "生产运营指标", description = "生产、原料消耗、库存和物流吞吐指标")
@CrossOrigin(origins = "*")
public class ProductionController {

    private final ProductionService productionService;

    @Operation(summary = "生产运营总览")
    @GetMapping("/overview")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_VIEW)")
    public Result<ProductionOverviewDTO> getOverview(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(productionService.getOverview(date));
    }

    @Operation(summary = "按物料组查询产量")
    @GetMapping("/output/by-material-group")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_VIEW)")
    public Result<List<ProductionRankItemDTO>> getOutputByMaterialGroup(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(productionService.getOutputByMaterialGroup(date));
    }

    @Operation(summary = "按物料查询原料消耗")
    @GetMapping("/raw-consumption/by-material")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_VIEW)")
    public Result<List<ProductionRankItemDTO>> getRawConsumptionByMaterial(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(productionService.getRawConsumptionByMaterial(date));
    }

    @Operation(summary = "产品库存")
    @GetMapping("/product-inventory")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_VIEW)")
    public Result<List<ProductionRankItemDTO>> getProductInventory(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(productionService.getProductInventory(date));
    }

    @Operation(summary = "按工厂查询车辆吞吐")
    @GetMapping("/throughput/by-factory")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_VIEW)")
    public Result<List<ProductionRankItemDTO>> getThroughputByFactory(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(productionService.getThroughputByFactory(date));
    }

    @Operation(summary = "产量明细")
    @GetMapping("/output/details")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_DETAIL_VIEW)")
    public Result<ProductionDetailPageDTO<ProductionOutputDetailDTO>> getOutputDetails(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String materialGroup,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(productionService.getOutputDetails(date, company, materialGroup, keyword, page, pageSize));
    }

    @Operation(summary = "原料消耗明细")
    @GetMapping("/raw-consumption/details")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_DETAIL_VIEW)")
    public Result<ProductionDetailPageDTO<ProductionRawConsumptionDetailDTO>> getRawConsumptionDetails(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String factory,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(productionService.getRawConsumptionDetails(date, factory, material, keyword, page, pageSize));
    }

    @Operation(summary = "车辆运输明细")
    @GetMapping("/transport/details")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).PRODUCTION_DETAIL_VIEW)")
    public Result<ProductionDetailPageDTO<ProductionTransportDetailDTO>> getTransportDetails(
            @NotNull(message = "日期不能为空")
            @RequestParam("date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(productionService.getTransportDetails(date, category, keyword, page, pageSize));
    }
}
