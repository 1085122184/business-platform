package com.cjx.decision.dto.system.dingtalk;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * DingTalk user batch import request.
 */
@Data
public class DingTalkUserImportRequest {

    @NotEmpty(message = "dingUserIds must not be empty")
    private List<String> dingUserIds;

    @NotEmpty(message = "roleIds must not be empty")
    private List<Long> roleIds;

    @NotNull(message = "status must not be null")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
