package com.cjx.decision.service;

import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;

import java.time.LocalDate;
import java.util.List;

/**
 * 价格分析服务
 * 负责查询价格偏差和客户交易数据
 * 
 * @author system
 * @version 1.0.0
 */
public interface PriceAnalysisService {
    
    /**
     * 查询价格偏差列表
     * @param date 日期
     * @return 价格偏差列表
     */
    List<RawPriceDeviation> getPriceDeviations(LocalDate date);
    
    /**
     * 查询客户交易详情
     * @param region 区域代码
     * @param code 产品代码
     * @return 客户交易列表
     */
    List<CustomerTransactionProjection> getCustomerTransactions(String region, String code, String type, LocalDate date);
}
