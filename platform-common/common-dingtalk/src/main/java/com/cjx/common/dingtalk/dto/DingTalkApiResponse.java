package com.cjx.common.dingtalk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 钉钉API通用响应DTO
 *
 * @param <T> 数据类型
 * @author system
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DingTalkApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("errcode")
    private Integer errCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonProperty("result")
    private T result;

    /**
     * 判断是否请求成功
     */
    public boolean isSuccess() {
        return errCode != null && errCode == 0;
    }
}
