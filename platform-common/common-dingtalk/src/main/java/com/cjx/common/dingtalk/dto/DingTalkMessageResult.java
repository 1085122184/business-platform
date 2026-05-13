package com.cjx.common.dingtalk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 钉钉消息发送结果DTO
 *
 * @author system
 */
@Data
public class DingTalkMessageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonProperty("task_id")
    private Long taskId;

    /**
     * 判断是否发送成功
     */
    public boolean isSuccess() {
        return errCode != null && errCode == 0;
    }
}
