package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.projection.frorcl.AllDetails;
import com.cjx.decision.service.AllDetailsService;
import com.cjx.decision.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/all_details")
@Validated
@RequiredArgsConstructor
@Tag(name = "获取明细", description = "获取各项明细")
@CrossOrigin(origins = "*")
public class AllDetailsController {
    private final DashboardService dashboardService;

    /**
     * 查询销售明细
     * @param companyName 公司名称
     * @param date 日期
     * @return 公司指标列表
     */
    @GetMapping("/sale_details")
    public Result<List<AllDetails>> getCompanyList(
            @RequestParam String companyName,
            @NotNull(message = "日期不能为空") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(dashboardService.findSalesDetail(date, companyName));
    }
}
