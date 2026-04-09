package com.cjx.uibot.service.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

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
public class UiBotProcessStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 流程状态 0-空闲 1-运行中
     */
    private String processStatus;

    /**
     * 当前流程id
     */
    private String uibotId;
}
