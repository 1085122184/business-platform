package com.cjx.decision.service;

import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.dashboard.SalesTrendProductDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;

import java.time.LocalDate;
import java.util.List;

/**
 * 趋势分析服务
 * 负责查询销售趋势和产品深度数据
 * 
 * @author system
 * @version 1.0.0
 */
public interface TrendAnalysisService {
    
    /**
     * 查询月度销售趋势
     * @param date 日期
     * @return 销售趋势列表
     */
    List<SalesTrendProductDTO> getMonthlyTrends(LocalDate date);
    
    /**
     * 查询年度销售趋势详情
     * @param productCode 产品代码
     * @param region 区域
     * @param date 日期
     * @return 销售趋势点列表
     */
    List<SalesTrendPointDTO> getYearlyTrends(String productCode, String region, LocalDate date);
    
    /**
     * 查询产品深度详情
     * @param companyName 公司名称
     * @param productCode 产品代码
     * @param type 分析类型(month/year)
     * @param date 日期
     * @return 产品深度详情
     */
    ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date);
}
