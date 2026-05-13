package com.cjx.decision.dto.system.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User item response for account management.
 */
@Data
public class UserResponse {

    private Long id;

    private String username;

    private String nickname;

    private String realName;

    private String email;

    private String mobile;

    private String dingUserId;

    private Integer status;

    private List<Long> roleIds = new ArrayList<>();

    private List<String> roleNames = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
