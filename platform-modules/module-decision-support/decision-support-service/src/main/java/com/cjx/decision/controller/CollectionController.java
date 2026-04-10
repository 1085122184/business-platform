package com.cjx.decision.controller;

import com.cjx.common.core.result.Result;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.service.CollectionService;
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
 * 回款分析Controller
 * 提供回款公司列表查询接口
 * 
 * @author cuijixu
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/collection-analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "回款分析", description = "查询回款公司列表数据")
@CrossOrigin(origins = "*")
public class CollectionController {
    
    private final CollectionService collectionService;
    
    /**
     * 查询回款公司列表
     * @param date 日期
     * @return 回款公司列表(value=当月回款, target=计划回款, companyName=公司名)
     */
    @GetMapping("/collection-companies")
    public Result<List<CompanyMetricDTO>> getCollectionCompanies(
            @NotNull(message = "日期不能为空") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(collectionService.getCollectionCompanies(date));
    }
}
