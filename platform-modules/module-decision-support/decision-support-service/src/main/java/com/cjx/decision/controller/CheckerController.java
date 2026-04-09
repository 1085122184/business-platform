package com.cjx.decision.controller;

import com.cjx.decision.service.SalesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author cuijixu
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
@Tag(name = "获取卡片指标数据", description = "获取卡片指标数据")
@CrossOrigin(origins = "*")
public class CheckerController {
    @Resource(name = "ioExecutor")
    private ThreadPoolTaskExecutor ioExecutor;

    private final SalesService salesService;

//    @GetMapping("/")
//    @Operation(summary = "获取卡片数据", description = "获取卡片数据")
//    public SalesSummary get(){
//        return salesService.findCountBudget();
//    }

}
