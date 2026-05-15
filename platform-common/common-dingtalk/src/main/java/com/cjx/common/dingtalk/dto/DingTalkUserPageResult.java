package com.cjx.common.dingtalk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DingTalk department user page result.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DingTalkUserPageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("list")
    private List<DingTalkUserInfo> list = new ArrayList<>();

    @JsonProperty("has_more")
    private Boolean hasMore;

    @JsonProperty("next_cursor")
    private Long nextCursor;
}
