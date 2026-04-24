package com.cjx.decision.service;

import com.cjx.decision.dto.expense.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 三费监控服务接口
 */
public interface ExpenseService {

    /**
     * 获取三费总览指标（包含金额、同比、占比）
     */
    ExpenseOverviewDTO getOverview(LocalDate date);

    /**
     * 获取三费占比结构数据
     */
    List<ExpenseStructureDTO> getStructure(LocalDate date);

    /**
     * 获取近12个月三费趋势数据
     */
    ExpenseTrendDTO getTrend(LocalDate date);

    /**
     * 获取 Top 10 公司三费对比数据
     */
    List<CompanyComparisonDTO> getComparison(LocalDate date);

    /**
     * 分页查询各公司三费明细
     * @param date 业务日期
     * @param keyword 搜索关键字（公司名称）
     * @param page 页码
     * @param pageSize 每页条数
     */
    CompanyDetailListDTO getCompanyDetail(LocalDate date, String keyword, Integer page, Integer pageSize);

    List<ExpenseDailyDetail> getDailyDetail(LocalDate date,String companyName);

    List<BudgetExecutionDTO> getBudgetExecution(LocalDate date,String dimension);

}