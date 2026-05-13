package com.cjx.decision.dto.ai;

import lombok.Data;

/**
 * @author cuijixu
 */
@Data
public class AiDiagnosisDTO {

    /**
     * 告警级别：success (达标) / info (正常) / warning (落后)
     */
    private String level;


    private String icon;

    /**
     * 标题， "提前达标", "产能严重滞后"
     */
    private String title;


    private String text;
}
