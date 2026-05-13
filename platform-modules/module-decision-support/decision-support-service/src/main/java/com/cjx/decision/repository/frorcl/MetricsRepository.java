package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/**
 * 核心指标Repository
 * 负责查询销量、销售额、回款、订单等核心指标数据
 * 
 * @author system
 * @version 1.0.0
 */
@org.springframework.stereotype.Repository
public interface MetricsRepository extends Repository<VLvlengRixiaoshou, Long> {
    
    /**
     * 查询昨日销售汇总
     */
    @Query(value = """
      SELECT ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
      FROM v_sales_detail_all
      WHERE 过账日期 = :targetDate AND 销量 <> 0
      """, nativeQuery = true)
    SalesSummary findSummaryByDate(@Param("targetDate") String targetDate);
    
    /**
     * 查询累计销售
     */
    @Query(value = """
      SELECT ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
      FROM v_sales_detail_all
      WHERE TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(TRUNC(:targetDate) - 1, 'MM') AND TO_DATE(过账日期, 'YYYY-MM-DD') < TRUNC(:targetDate) AND 销量 <> 0
      """, nativeQuery = true)
    SalesSummary findSummaryToToday(@Param("targetDate") LocalDate targetDate);
    
    /**
     * 查询销量预算
     */
    @Query(value = """
      SELECT 月份,ROUND(SUM(销量预算),2) totalCountBudget FROM V_SALES_BUDGET_SUMMARY WHERE 月份 = :targetDate GROUP BY 月份
      """, nativeQuery = true)
    SalesSummary findCountBudget(@Param("targetDate") String targetDate);
    
    /**
     * 查询销售额预算
     */
    @Query(value = """
      SELECT 月份,ROUND(SUM(销售额预算),2) totalAmountBudget FROM V_SALES_BUDGET_SUMMARY WHERE 月份 = :targetDate GROUP BY 月份
      """, nativeQuery = true)
    SalesSummary findAmountBudget(@Param("targetDate") String targetDate);
    
    /**
     * 查询回款
     */
    @Query(value = """
      SELECT SUM(回款) AS collection FROM
      (
      SELECT SUM(回款) AS 回款,'绿冷' AS 公司
      FROM v_lvleng_huikuan
      WHERE 渠道 <> '关联'AND 年度 = TO_CHAR(:targetDate, 'yyyy')AND 期间 = TO_CHAR(:targetDate, 'MM')
      UNION ALL
      SELECT SUM(回款) AS 回款,'高分子' AS 公司
      FROM v_gfz_huikuan
      WHERE 渠道 <> '关联' AND 财年 = TO_CHAR(:targetDate, 'yyyy') AND 过账期间 = TO_CHAR(:targetDate, 'MM')
      UNION ALL
      SELECT SUM(回款) AS 回款,'氟硅' AS 公司
      FROM v_fg_huikuan
      WHERE 渠道 <> '关联' AND 财年 = TO_CHAR(:targetDate, 'yyyy') AND 过账期间 = TO_CHAR(:targetDate, 'MM')
      UNION ALL
      SELECT SUM(回款) AS 回款,'有机硅' AS 公司 from V_YJG_HUIKUAN
      WHERE 渠道 <> '关联' AND TO_CHAR(时间戳, 'yyyy-MM') = TO_CHAR(:targetDate, 'yyyy-MM')
      )
      """, nativeQuery = true)
    SalesSummary findCollection(@Param("targetDate") LocalDate targetDate);
    
    /**
     * 查询月订单
     */
    @Query(value = """
      SELECT
          SUM(总订单数) AS totalOrder,
          SUM(关闭订单数) AS closedOrder,
          SUM(非关闭状态订单数) AS openOrder
      FROM (
          -- 第一部分：当月全量数据
          SELECT
              COUNT(1) AS 总订单数,
              COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 关闭订单数,
              COUNT(1) - COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 非关闭状态订单数
          FROM so_order
          WHERE 创建日期 >=:thisMonth            -- 月初 00:00:00
            AND 创建日期 < :lastMonth -- 下月初 00:00:00 (替代 LAST_DAY 以保性能)
            AND 工厂 IN ('1301', '1201', '3001')

          UNION ALL

          -- 第二部分：指定日期当天数据
          SELECT
              COUNT(1) AS 总订单数,
              COUNT(CASE WHEN 单据状态 = '关闭' THEN 1 END) AS 关闭订单数,
              COUNT(*) - COUNT(CASE WHEN 单据状态 = '关闭' THEN 1 END) AS 非关闭状态订单数
          FROM v_yjg_order_qty
          WHERE
            公司编码 ='1400' AND TO_CHAR(时间戳, 'YYYY-MM-DD') = TO_CHAR(TRUNC(:targetDate), 'YYYY-MM-DD')
      )
      """, nativeQuery = true)
    SalesSummary findMonthOrder(@Param("thisMonth") String thisMonth, @Param("lastMonth") String lastMonth, @Param("targetDate") LocalDate targetDate);
    
    /**
     * 查询年订单
     */
    @Query(value = """
      WITH daily_status AS (
                             -- 按天和状态统计 DWM_V_JT_QTY 中当天的累计订单数
                             SELECT
                                 TO_CHAR(时间戳, 'YYYY-MM-DD') AS 创建日期,
                                 单据状态,
                                 COUNT(*) AS 总订单数
                             FROM v_yjg_order_qty
                             WHERE TO_CHAR(时间戳, 'YYYY-MM-DD') >= SUBSTR(:targetDate,1,4)  -- 今年第一天
                               AND TO_CHAR(时间戳, 'YYYY-MM-DD') <= :targetDate       -- 今天
                               AND 公司编码 = '1400'
                             GROUP BY TO_CHAR(时间戳, 'YYYY-MM-DD'), 单据状态
                         ),
                         monthly_last AS (
                             -- 对每个月份和状态，取该月最新一天（最后一天）的记录
                             SELECT
                                 单据状态,
                                 总订单数,
                                 ROW_NUMBER() OVER (
                                     PARTITION BY 单据状态,SUBSTR(创建日期,6,2)  -- 注意：这里去掉了多余的 's'
                                     ORDER BY 创建日期 DESC
                                 ) AS rn
                             FROM daily_status
                         )
                         SELECT
                             SUM(总订单数) AS totalOrder,
                             SUM(关闭订单数) AS closedOrder,
                             SUM(非关闭状态订单数) AS openOrder
                         FROM (
                             -- 第一个分支：从 so_order 直接统计（普通订单表）
                             SELECT
                                 COUNT(*) AS 总订单数,
                                 COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 关闭订单数,
                                 COUNT(*) - COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 非关闭状态订单数
                             FROM so_order
                             WHERE 创建日期 >= :thisYear
                               AND 工厂 IN ('1301', '1201', '3001')

                             UNION ALL

                             -- 第二个分支：从 DWM_V_JT_QTY 按月累计统计（每日快照表）
                             SELECT
                                 SUM(总订单数) AS 总订单数,
                                 SUM(CASE WHEN 单据状态 = '关闭' THEN 总订单数 ELSE 0 END) AS 关闭订单数,
                                 SUM(总订单数) - SUM(CASE WHEN 单据状态 = '关闭' THEN 总订单数 ELSE 0 END) AS 非关闭状态订单数
                             FROM monthly_last
                             WHERE rn = 1
                         )
      """, nativeQuery = true)
    SalesSummary findYearOrder(@Param("thisYear") String thisYear, @Param("targetDate") String targetDate);
}
