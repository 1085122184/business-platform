package com.cjx.common.dingtalk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 钉钉部门信息DTO
 *
 * @author system
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DingTalkDeptInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("dept_id")
    private Long deptId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parent_id")
    private Long parentId;

    @JsonProperty("create_dept_group")
    private Boolean createDeptGroup;

    @JsonProperty("dept_manager_userid_list")
    private java.util.List<String> deptManagerUserIdList;

    @JsonProperty("outer_dept")
    private Boolean outerDept;

    @JsonProperty("outer_permit_depts")
    private java.util.List<Long> outerPermitDepts;
}
