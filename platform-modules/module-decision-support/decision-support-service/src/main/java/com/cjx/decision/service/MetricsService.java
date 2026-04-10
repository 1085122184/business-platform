package com.cjx.decision.service;

import com.cjx.decision.dto.dashboard.DashboardMetricsDTO;
import com.cjx.decision.dto.dashboard.DashboardOrdersDTO;

import java.time.LocalDate;

/**
 * 核心指标服务
 * 负责查询销量、销售额、回款、订单等核心指标
 * 
 * @author system
 * @version 1.0.0
 */
public interface MetricsService {
    
    /**
     * 查询核心指标
     * @param date 日期
     * @return 核心指标DTO
     */
    DashboardMetricsDTO getMetrics(LocalDate date);
    
    /**
     * 查询订单数据
     * @param date 日期
     * @return 订单数据DTO
     */
    DashboardOrdersDTO getOrders(LocalDate date);
}
