package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.expense.*;
import com.cjx.decision.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "三费监控模块")
@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "总览指标查询")
    @GetMapping("/overview")
    public Result<ExpenseOverviewDTO> getOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(expenseService.getOverview(date));
    }

    @Operation(summary = "公司明细列表查询")
    @GetMapping("/company-detail")
    public Result<CompanyDetailListDTO> getCompanyDetail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(expenseService.getCompanyDetail(date, keyword, page, pageSize));
    }

    @Operation(summary = "公司对比查询")
    @GetMapping("/company-comparison")
    public Result<List<CompanyComparisonDTO>> getComparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(expenseService.getComparison(date));
    }

    @Operation(summary = "获取费用结构数据")
    @GetMapping("/structure")
    public Result<List<ExpenseStructureDTO>> getStructure(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(expenseService.getStructure(date));
    }

    @Operation(summary = "获取三费趋势数据")
    @GetMapping("/trend")
    public Result<ExpenseTrendDTO> getTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(expenseService.getTrend(date));
    }

    @Operation(summary = "获取三费趋势数据")
    @GetMapping("/budget-execution")
    public Result<List<BudgetExecutionDTO>> getBudgetExecution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String dimension) {
        return Result.success(expenseService.getBudgetExecution(date,dimension));
    }


}