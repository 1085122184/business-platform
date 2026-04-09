package com.cjx.uibot.service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 崔吉旭
 * @since 2025-11-03
 */
@Getter
@Setter
public class UiBotProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creatTime;

    /**
     * 结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 流程名称
     */
    private String triggerName;

    /**
     * 流程状态 0-排队中 1-执行中 2-执行完毕 3-执行失败
     */
    private String status;
}
