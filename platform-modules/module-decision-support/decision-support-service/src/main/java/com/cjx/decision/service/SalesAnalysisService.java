package com.cjx.decision.service;

import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 销售分析服务
 * 负责查询销售明细和公司详情数据
 * 
 * @author system
 * @version 1.0.0
 */
public interface SalesAnalysisService {
    
    /**
     * 查询销售公司列表
     * @param type 指标类型(volume/amount)
     * @param date 日期
     * @return 公司指标列表
     */
    List<CompanyMetricDTO> getCompanyList(String type, LocalDate date);
    
    /**
     * 查询销售公司详情
     * @param companyName 公司名称
     * @param type 指标类型(volume/amount)
     * @param date 日期
     * @return 公司详情
     */
    List<CompanyDetailDTO> getCompanyDetail(String companyName, String type, LocalDate date);
}
