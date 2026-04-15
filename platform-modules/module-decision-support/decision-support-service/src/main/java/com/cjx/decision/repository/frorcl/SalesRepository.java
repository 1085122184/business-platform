package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 只读Repository，用于查询销售相关数据。
 * 仅提供查询方法，不支持增删改操作。
 *
 * @deprecated 已按业务域拆分为 MetricsRepository、PriceAnalysisRepository、SalesAnalysisRepository、TrendAnalysisRepository。
 *             请迁移到对应的专用Repository。此接口保留仅作过渡期兼容使用。
 *
 * @author cuijixu
 */
@Deprecated
@org.springframework.stereotype.Repository
public interface SalesRepository extends Repository<VLvlengRixiaoshou, Long> {


    @Query(value = """
      SELECT ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount 
      FROM v_sales_detail_all 
      WHERE 过账日期 = :targetDate AND 销量 <> 0
      """, nativeQuery = true)
    SalesSummary findSummaryByDate(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount 
      FROM v_sales_detail_all 
      WHERE TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(TRUNC(:targetDate) - 1, 'MM') AND TO_DATE(过账日期, 'YYYY-MM-DD') < TRUNC(:targetDate) AND 销量 <> 0
      """, nativeQuery = true)
    SalesSummary findSummaryToToday(@Param("targetDate") LocalDate targetDate);

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

    @Query(value = """
      SELECT  
      CASE 渠道
        WHEN '10' THEN '国内'
        WHEN '20' THEN '国外'
      END AS region,
      物料组 AS productCode,物料组描述 AS productName,过账日期 AS latestDate,
      ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
      ROUND(SUM(NVL(金额, 0)), 2)  AS totalAmount,
      ROUND(CASE WHEN SUM(NVL(销量, 0)) = 0 THEN 0 ELSE SUM(NVL(金额, 0)) / SUM(NVL(销量, 0))*1000 END,2) AS price
      FROM v_sales_detail_all
      WHERE 过账日期 = :targetDate GROUP BY 渠道,物料组,物料组描述,过账日期
      """, nativeQuery = true)
    List<SalesSummary> findTrendsToday(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT 工厂,
      CASE 渠道
              WHEN '10' THEN '国内'
              WHEN '20' THEN '国外'
            END AS region,
      物料组 AS productCode,物料组描述 AS productName,过账日期 AS latestDate,
      ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
      ROUND(SUM(NVL(金额, 0)), 2)  AS totalAmount,
      ROUND(CASE WHEN SUM(NVL(销量, 0)) = 0 THEN 0 ELSE SUM(NVL(金额, 0)) / SUM(NVL(销量, 0))*10000 END,2) AS price
      FROM v_sales_detail_all
      WHERE 过账日期 BETWEEN :startDate AND :endDate AND 物料组描述 <> '无价值物料' AND 销售主产 = '1'
      GROUP BY 工厂,渠道,物料组,物料组描述,过账日期
            """, nativeQuery = true)
    List<SalesSummary> findTrendsAll(@Param("startDate") String startDate,@Param("endDate") String endDate);

    @Query(value = """
       SELECT 公司编码,渠道 AS region,
                  物料组 AS productCode,物料组描述 AS productName,TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM') AS latestDate,
                  ROUND(SUM(NVL(开票量, 0)), 2) AS totalSales,
                  ROUND(SUM(NVL(开票金额, 0)), 2)  AS totalAmount,
                  ROUND(CASE WHEN SUM(NVL(开票量, 0)) = 0 THEN 0 ELSE SUM(NVL(开票金额, 0)) / SUM(NVL(开票量, 0))*10000 END,2) AS price
                  FROM dwm_v_jt_fapiao_mingxi
                  WHERE 物料组 = :productCode AND 渠道 = :region AND 物料组描述 <> '无价值物料' AND 日期 BETWEEN :startDate AND :endDate
                  GROUP BY 公司编码,渠道,物料组,物料组描述,TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM') ORDER BY TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM')
      """, nativeQuery = true)
    List<SalesSummary> findTrendsYear(@Param("startDate") String startDate,@Param("endDate") String endDate,@Param("productCode") String productCode,@Param("region") String region);

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
    List<SalesSummary> findDetailsByCompany(@Param("targetDate") LocalDate targetDate,@Param("company") String company);

    @Query(value = """
      SELECT
          CASE 工厂 WHEN '1201' THEN '高分子'
          WHEN '1301' THEN '氟硅'
          WHEN '3001' THEN '绿冷'
          WHEN '1400' THEN '有机硅'
          END AS companyName,
          ROUND(SUM(NVL(销量, 0)), 2) AS totalSales
          FROM v_sales_detail_all
          WHERE 过账日期 = :targetDate AND 物料组描述 <> '无价值物料' AND 销量 <> 0 GROUP BY 工厂
      """, nativeQuery = true)
    List<SalesSummary> findSummaryByDateDetails(@Param("targetDate") String targetDate);



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

    @Query(value = """
      SELECT 月份,ROUND(SUM(销量预算),2) totalCountBudget FROM V_SALES_BUDGET_SUMMARY WHERE 月份 = :targetDate GROUP BY 月份
      """, nativeQuery = true)
    SalesSummary findCountBudget(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT 月份,公司 AS companyName,ROUND(SUM(销量预算),2) totalCountBudget FROM V_SALES_BUDGET_SUMMARY WHERE 月份 = :targetDate GROUP BY 月份,公司
      """, nativeQuery = true)
    List<SalesSummary> findCountBudgetDetails(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT SUM(国内销售额预算+国外销售额预算) AS totalAmountBudget FROM
      (
      SELECT '绿冷' AS 公司,ROUND(SUMGUONEI,2) AS 国内销售额预算,ROUND(SUMGUOWAI,2) AS 国外销售额预算 FROM ys_xssr_hg WHERE QIJIAN = :targetDate
      union all
      SELECT '高分子' AS 公司,ROUND(GUONEI,2) AS 国内销售额预算,ROUND(GUOWAI,2) AS 国外销售额预算 FROM ys_xssr_gfz WHERE QIJIAN = :targetDate
      union all
      SELECT '氟硅' AS 公司,ROUND(LVJIAWANNEI ,2)+ROUND(LVJIANNEI ,2) AS 国内销售额预算,ROUND(LVJIAWANWAI ,2)+ROUND(LVJIANWAI ,2) AS 国外销售额预算 FROM ys_xssr_fg  WHERE QIJIAN = :targetDate
      union all
      SELECT '有机硅' AS 公司,ROUND(GUONEI,2) AS 国内销售额预算,ROUND(GUOWAI,2) AS 国外销售额预算 FROM YS_XSSR_YJG  WHERE QIJIAN = :targetDate
      )
      """, nativeQuery = true)
    SalesSummary findAmountBudget(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT 公司 AS companyName,SUM(国内销售额预算+国外销售额预算) AS totalAmountBudget FROM v_sales_budget WHERE QIJIAN = :targetDate GROUP BY 公司
      """, nativeQuery = true)
    List<SalesSummary> findAmountBudgetDetails(@Param("targetDate") String targetDate);

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
    SalesSummary findMonthOrder(@Param("thisMonth") String thisMonth,@Param("lastMonth") String lastMonth,@Param("targetDate")LocalDate targetDate);


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
    SalesSummary findYearOrder(@Param("thisYear") String thisYear,@Param("targetDate") String targetDate);

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

    @Query(value = """
      SELECT 
      t.物料组 AS productCode,
      t.物料组描述 AS productName,
      CASE t.渠道
        WHEN '10' THEN '国内'
        WHEN '20' THEN '国外'
      END AS region,
      t.七日均价 AS avgPrice7d,
      t.当日单价 AS todayPrice,
      t.价差偏移 AS deviationAmt,
      CASE
        WHEN t.七日均价 = 0 OR t.七日均价 IS NULL THEN 0
        ELSE ROUND((t.当日单价 - t.七日均价) / t.七日均价 * 100, 2)
      END AS deviationPct
      FROM (
      SELECT
      工厂,物料组,物料组描述,渠道,
      ROUND(SUM(金额) / NULLIF(SUM(销量), 0) * 10000, 2) AS 七日均价,
      ROUND(SUM(CASE WHEN 过账日期 = :endDate THEN 金额 ELSE 0 END) /NULLIF(SUM(CASE WHEN 过账日期 = :endDate THEN 销量 ELSE 0 END), 0) * 10000,2) AS 当日单价,
      ROUND(SUM(CASE WHEN 过账日期 = :endDate THEN 金额 ELSE 0 END) /NULLIF(SUM(CASE WHEN 过账日期 = :endDate THEN 销量 ELSE 0 END), 0) * 10000,2) - ROUND(SUM(金额) / NULLIF(SUM(销量), 0) * 10000, 2) AS 价差偏移
      FROM v_sales_detail_all
      WHERE 物料组描述 <> '无价值物料' AND 销售主产 = '1' AND 过账日期 BETWEEN :startDate AND :endDate GROUP BY 工厂,物料组, 物料组描述, 渠道) t WHERE 当日单价 IS NOT NULL ORDER BY deviationPct
      """, nativeQuery = true)
    List<RawPriceDeviation> findPriceDiff(@Param("startDate") String startDate,@Param("endDate") String endDate);

    @Query(value = """
      SELECT SUM(销量) AS volume,ROUND(SUM(金额*10000)/SUM(销量),2) AS price,客户名称 AS customer
      FROM v_sales_detail_all
      WHERE 过账日期 = :targetDate AND 渠道 = :region AND 物料组= :code AND 物料组描述 <> '无价值物料' AND 销量 <> 0
      GROUP BY 客户名称
      """, nativeQuery = true)
    List<CustomerTransactionProjection> findCustomerTransaction(@Param("region") String region,@Param("code") String code,@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT 渠道 AS region,物料组 AS productCode,物料组描述 AS productName,SUM(销量) AS totalSales,SUM(金额) AS totalAmount,过账日期 AS latestDate FROM v_sales_detail_all
      WHERE 公司编码 = :companyName AND 物料组 = :productCode 
      AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate-1, 'MM')
      AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate-1)
      GROUP BY 渠道,物料组,物料组描述,过账日期
      ORDER BY 过账日期
      """, nativeQuery = true)
    List<SalesSummary> getProductDeepMonth(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);

    @Query(value = """
      SELECT 渠道 AS region,物料组 AS productCode,物料组描述 AS productName,SUM(开票量) AS totalSales,SUM(开票金额) AS totalAmount,SUBSTR(日期,1,7)  AS latestDate FROM dwm_v_jt_fapiao_mingxi
                  WHERE 公司编码 = :companyName AND 物料组  = :productCode AND 渠道 <> '关联'
                  AND SUBSTR(日期,1,4) = TO_CHAR(TRUNC(:targetDate-1), 'YYYY')
                  GROUP BY 渠道,物料组,物料组描述,SUBSTR(日期,1,7)
                  ORDER BY SUBSTR(日期,1,7)
      """, nativeQuery = true)
    List<SalesSummary> getProductDeepYear(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);



    @Query(value = """
      SELECT 客户名称 AS customer,渠道 AS region,物料组 AS productCode,物料组描述 AS productName,SUM(开票量) AS totalSales,SUM(开票金额) AS totalAmount FROM dwm_v_jt_fapiao_mingxi
                  WHERE 公司编码 = :companyName AND 物料组 = :productCode AND 渠道 <> '关联'
                  AND SUBSTR(日期,1,4) = TO_CHAR(TRUNC(:targetDate-1), 'YYYY')
                  GROUP BY 客户名称,渠道,物料组,物料组描述
                  ORDER BY totalSales DESC
      """, nativeQuery = true)
    List<SalesSummary> getProductCustomer(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);



}
