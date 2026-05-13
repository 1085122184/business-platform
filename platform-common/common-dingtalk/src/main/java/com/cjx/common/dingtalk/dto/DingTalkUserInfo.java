package com.cjx.common.dingtalk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 钉钉用户信息DTO
 *
 * @author system
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DingTalkUserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("userid")
    private String userId;

    @JsonProperty("unionid")
    private String unionId;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("email")
    private String email;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("dept_id_list")
    private List<Long> deptIdList;

    @JsonProperty("position")
    private String position;

    @JsonProperty("job_number")
    private String jobNumber;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("senior")
    private Boolean senior;

    @JsonProperty("sys")
    private Boolean sys;

    @JsonProperty("sys_level")
    private Integer sysLevel;
}
