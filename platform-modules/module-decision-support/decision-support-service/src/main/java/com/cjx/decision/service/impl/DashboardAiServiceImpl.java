package com.cjx.decision.service.impl;

import com.cjx.common.ai.service.AiChatService;
import com.cjx.common.ai.util.PromptUtils;
import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineUtil;
import com.cjx.decision.dto.ai.AiDiagnosisDTO;
import com.cjx.decision.service.DashboardAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author cuijixu
 */
@Service
@RequiredArgsConstructor
public class DashboardAiServiceImpl implements DashboardAiService {
    private final AiChatService aiChatService;

    private final CaffeineUtil caffeineUtil;
    @Override
    public AiDiagnosisDTO generateDiagnosis(String companyName, BigDecimal value, BigDecimal target, String unit, LocalDate localDate,String bizType) {
        AiDiagnosisDTO aiDiagnosisDTO = new AiDiagnosisDTO();
        if ("sales".equals(bizType)){
            aiDiagnosisDTO = getAiDiagnosisDTO(companyName, value, target, unit, localDate);
        }else {

        }
        return aiDiagnosisDTO;
    }

    private AiDiagnosisDTO getAiDiagnosisDTO(String companyName, BigDecimal value, BigDecimal target, String unit, LocalDate localDate) {
        String date = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        BigDecimal gap = target.subtract(value);
        // 🌟 1. 把包含大括号的 JSON 示例单独拿出来，作为一个普通的 Java 字符串
        String jsonFormatExample = """
                {
                  "level": "warning",
                  "icon": "⚠️",
                  "title": "产能滞后预警",
                  "text": "你的分析正文..."
                }
                """;

        // 🌟 2. 模板中用 {jsonExample} 占位，避免直接在模板里写大括号
        String promptTemplate = """
                你是一位资深的企业经营数据分析专家。
                请根据以下销售进度数据，生成一段高管视角的经营诊断分析报告。
                                
                【当前数据】
                - 分析主体：{companyName}
                - 截止日期：{date}（格式：YYYY-MM-DD）
                - 本月已完成的总业绩：{value} {unit}
                - 本月总目标：{target} {unit}
                - 目标差距：{gap} {unit}（已计算为 target - value，正值表示落后，负值表示超额）
                                
                【分析步骤（必须严格执行）】
                1. 根据 {date} 确定当月总天数（注意闰年，2月为28或29天）。
                2. 计算“本月已过天数”：截止日期当天视为已过（例如4月7日，已过天数为7）。
                3. 计算“剩余天数” = 当月总天数 - 已过天数（若截止日期为当月最后一天，剩余天数为0）。
                4. 若 {gap} <= 0，表示已达成或超额目标，直接进入输出（无需计算日均）。
                5. 若 {gap} > 0 且剩余天数 > 0：
                   - 计算“所需剩余日均业绩” = {gap} / 剩余天数。
                   - 计算“当前日均业绩” = {value} / 已过天数。
                   - 对比两者，判断是否可达成，并给出冲刺建议。
                6. 若 {gap} > 0 且剩余天数 == 0，直接判定无法完成。
                                
                【输出要求】
                1. 语气专业、精炼，直击痛点。根据分析结果给出明确结论：能否完成？若不能，落后多少？需要日均提升多少？
                2. 如果 {gap} <= 0：给予肯定，并预测月末最终超额量（按当前日均推算剩余天数业绩）。
                3. 如果 {gap} > 0：指出目前的落后程度（百分比或绝对值），并强烈建议提升日均产能，必须给出具体的“剩余日均需达成”数值。
                4. 在 text 字段中，必须使用 HTML 标签高亮核心数据，例如：
                   - <b>粗体</b> 强调关键结论
                   - <span style="color:#f56565">红色</span> 突出差距、落后值
                   - <span style="color:#38a169">绿色</span> 突出超额、完成值
                5. level 字段根据以下规则选择其一：
                   - "success"：{gap} <= 0 或预估可超额完成
                   - "warning"：{gap} > 0 但剩余日均缺口在可冲刺范围内（如需提升 < 50%）
                   - "info"：{gap} > 0 且剩余日均缺口极大（如需提升 > 100%）或剩余天数=0
                6. icon 字段：结合状态给出一个 Emoji，例如 ✅、⚠️、📉、🎯、🔥 等。
                                
                【极其重要：格式限制】
                你必须且只能输出一个合法的 JSON 对象！
                绝对不要包含任何 Markdown 标记（如 ```json 或 ```），绝对不要包含任何开头或结尾的说明文字！
                必须严格符合以下 JSON 结构：
                {jsonExample}
                """;

        // 🌟 3. 将 jsonExample 塞进变量 Map 里传给底层模板渲染
        Map<String, Object> vars = Map.of(
                "companyName", companyName,
                "date", date,
                "value", value,
                "target", target,
                "unit", unit,
                "gap", gap,
                "jsonExample", jsonFormatExample // 这里注入 JSON 示例
        );

        String finalPrompt = PromptUtils.render(promptTemplate, vars);
        String key = "company_diagnosis:"+ companyName + ":" + date+":" + unit;
        AiDiagnosisDTO aiDiagnosisDTO = caffeineUtil.getOrLoad(CacheType.TODAY_DATA,key,k ->aiChatService.chatAs(finalPrompt, AiDiagnosisDTO.class),AiDiagnosisDTO.class);
        return aiDiagnosisDTO;
    }
}
