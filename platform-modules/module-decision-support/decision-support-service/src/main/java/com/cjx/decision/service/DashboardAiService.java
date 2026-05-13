package com.cjx.decision.service;

import com.cjx.decision.dto.ai.AiDiagnosisDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DashboardAiService {
    AiDiagnosisDTO generateDiagnosis(String companyName, BigDecimal value, BigDecimal target, String unit, LocalDate date,String bizType);
}
