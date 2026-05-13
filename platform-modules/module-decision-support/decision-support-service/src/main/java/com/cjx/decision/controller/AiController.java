package com.cjx.decision.controller;

import com.cjx.common.ai.model.AiChatRequest;
import com.cjx.common.ai.service.AiChatService;
import com.cjx.common.core.result.Result;
import com.cjx.decision.constant.DecisionPermissions;
import com.cjx.decision.dto.ai.AiDiagnosisDTO;
import com.cjx.decision.service.DashboardAiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author cuijixu
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard/ai")
@RequiredArgsConstructor
@Validated
@Tag(name = "AI调用", description = "AI调用")
@CrossOrigin(origins = "*")
public class AiController {
    private final AiChatService aiChatService;
    private final DashboardAiService dashboardAiService;

    @PostMapping(value = "/insight/price-deviation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).AI_INSIGHT)")
    public Flux<String> getPriceDeviationInsight(@RequestBody List<Map<String, Object>> chartData) {

        String prompt = "你是一个资深大客户销售总监。请根据以下我系统里的【价格偏差数据 JSON】，" +
                "用 3 句话为我总结今日价格异常的重灾区。要求：\n" +
                "1. 直接指出哪几个产品跌破了七日均价，是国内还是国外市场。\n" +
                "2. 语气专业、简明扼要，不要包含 Markdown 的代码块符号。\n" +
                "数据内容如下：\n" + chartData.toString();

        return aiChatService.stream(AiChatRequest.of(prompt));
    }

    @GetMapping(value = "/company-diagnosis")
    @PreAuthorize("@ss.hasPermi(T(com.cjx.decision.constant.DecisionPermissions).AI_DIAGNOSIS)")
    public Result<AiDiagnosisDTO> getCompanyDiagnosis(
            @RequestParam String companyName,
            @RequestParam BigDecimal value,
            @RequestParam BigDecimal target,
            @RequestParam String unit,
            @RequestParam LocalDate date,
            @RequestParam String bizType) {
        AiDiagnosisDTO diagnosis = dashboardAiService.generateDiagnosis(companyName, value, target, unit, date,bizType);
        return Result.success(diagnosis);
    }

}
