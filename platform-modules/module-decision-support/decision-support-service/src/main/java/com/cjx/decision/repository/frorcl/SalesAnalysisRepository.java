package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 销售分析Repository
 * 负责查询销售明细和预算明细数据
 * 
 * @author system
 * @version 1.0.0
 */
@org.springframework.stereotype.Repository
public interface SalesAnalysisRepository extends Repository<VLvlengRixiaoshou, Long> {
    
    /**
     * 查询销量预算明细
     */
    @Query(value = """
      SELECT 月份,公司 AS companyName,ROUND(SUM(销量预算),2) totalCountBudget FROM V_SALES_BUDGET_SUMMARY WHERE 月份 = :targetDate GROUP BY 月份,公司
      """, nativeQuery = true)
    List<SalesSummary> findCountBudgetDetails(@Param("targetDate") String targetDate);
    
    /**
     * 查询销售额预算明细
     */
    @Query(value = """
      SELECT 公司 AS companyName,SUM(国内销售额预算+国外销售额预算) AS totalAmountBudget FROM v_sales_budget WHERE QIJIAN = :targetDate GROUP BY 公司
      """, nativeQuery = true)
    List<SalesSummary> findAmountBudgetDetails(@Param("targetDate") String targetDate);
    
    /**
     * 查询今日销售明细
     */
    @Query(value = """
      SELECT
          CASE 工厂 WHEN '1201' THEN '高分子'
          WHEN '1301' THEN '氟硅'
          WHEN '3001' THEN '绿冷'
          WHEN '1400' THEN '有机硅'
          END AS companyName,
          ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
          FROM v_sales_detail_all
      WHERE TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(TRUNC(:targetDate) - 1, 'MM') AND TO_DATE(过账日期, 'YYYY-MM-DD') < TRUNC(:targetDate)
      AND 工厂 IS NOT NULL AND 销量 <> 0  GROUP BY 工厂
      """, nativeQuery = true)
    List<SalesSummary> findSummaryTodayDetails(@Param("targetDate") LocalDate targetDate);
    
    /**
     * 查询公司销售趋势
     */
    @Query(value = """
      WITH date_range AS (
          -- 生成本月1号到昨天的所有日期（字符串格式 YYYY-MM-DD）
          SELECT TO_CHAR(TRUNC(:targetDate-1, 'MM') + LEVEL - 1, 'YYYY-MM-DD') AS dt
          FROM DUAL
          CONNECT BY LEVEL <= TRUNC(:targetDate) - TRUNC(:targetDate-1, 'MM')
      ),
      agg AS (
          -- 原汇总查询（简化日期条件）
          SELECT 过账日期,
                 ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
                 ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
          FROM v_sales_detail_all
          WHERE 公司编码 = :company
            AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate-1, 'MM')
            AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate-1)
          GROUP BY 过账日期
      )
      SELECT d.dt AS 过账日期,
             NVL(a.totalSales, 0) AS totalSales,
             NVL(a.totalAmount, 0) AS totalAmount
      FROM date_range d
      LEFT JOIN agg a ON d.dt = a.过账日期
      ORDER BY d.dt
      """, nativeQuery = true)
    List<SalesSummary> findSummaryByCompany(@Param("targetDate") LocalDate targetDate,@Param("company") String company);
    
    /**
     * 查询公司详情
     */
    @Query(value = """
      SELECT
      物料组描述 AS productName,物料组 AS productCode,
      ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
      ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount,
      -- 销量占比：直接除以全表总销量（SUM OVER 不需要分组，直接算全盘）
      ROUND(
        SUM(NVL(销量, 0)) / NULLIF(SUM(SUM(NVL(销量, 0))) OVER(), 0) * 100,
      2) AS salesRatio,
      -- 销售额占比
      ROUND(
        SUM(NVL(金额, 0)) / NULLIF(SUM(SUM(NVL(金额, 0))) OVER(), 0) * 100,
      2) AS amountRatio,
      CASE 渠道 WHEN '10' THEN '国内' WHEN '20' THEN '国外' END AS region
      FROM v_sales_detail_all
      WHERE 公司编码 = :company
        AND TO_DATE(过账日期, 'YYYY-MM-DD') = TRUNC(:targetDate-1) AND 物料组描述 <> '无价值物料' AND 销售主产 = '1'
      GROUP BY 渠道,物料组描述,物料组
      """, nativeQuery = true)
    List<SalesSummary> findDetailsByCompany(@Param("targetDate") LocalDate targetDate, @Param("company") String company);
}
