package com.cjx.decision.service;

import com.cjx.decision.dto.dashboard.*;
import com.cjx.decision.dto.salesdetail.CompanyDetailDTO;
import com.cjx.decision.dto.salesdetail.CompanyMetricDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.projection.frorcl.AllDetails;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.OrderDetail;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard服务门面
 * 组合各个子服务,提供统一的接口
 * 
 * @author system
 * @version 1.0.0
 */
public interface DashboardService {
    
    // ===== 核心指标 =====
    
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
    
    // ===== 价格分析 =====

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
    List<CustomerTransactionProjection> findCustomerTransaction(String region, String code, String type, LocalDate date);
    
    // ===== 销售分析 =====

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
    
    // ===== 趋势分析 =====

    /**
     * 查询月度销售趋势
     * @param date 日期(未使用,默认查询到昨天)
     * @return 销售趋势列表
     */
    List<SalesTrendProductDTO> getSalesTrends(LocalDate date);

    /**
     * 查询年度销售趋势详情
     * @param productCode 产品代码
     * @param region 区域
     * @param date 日期
     * @return 销售趋势点列表
     */
    List<SalesTrendPointDTO> getSalesTrendsList(String productCode, String region, LocalDate date);

    /**
     * 查询产品深度详情
     * @param companyName 公司名称
     * @param productCode 产品代码
     * @param type 分析类型(month/year)
     * @param date 日期
     * @return 产品深度详情
     */
    ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date);

    // ===== 回款分析 =====

    /**
     * 查询回款公司列表
     * @param date 日期
     * @return 回款公司列表(value=当月回款, target=计划回款, companyName=公司名)
     */
    List<CompanyMetricDTO> getCollectionCompanies(LocalDate date);

    // ===== 订单分析 =====

    /**
     * 查询订单公司详情
     * @param companyName 公司名称
     * @param date 日期
     * @return 订单详情
     */
    List<OrderDetailDTO> getOrderCompanyDetails(String companyName, LocalDate date);


    List<AllDetails> findSalesDetail(@Param("targetDate") LocalDate targetDate, @Param("companyCode") String companyCode);

}
