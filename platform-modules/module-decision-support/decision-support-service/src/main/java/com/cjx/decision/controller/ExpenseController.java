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
}