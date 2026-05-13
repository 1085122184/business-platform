package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 价格分析Repository
 * 负责查询价格偏差和客户交易数据
 * 
 * @author system
 * @version 1.0.0
 */
@org.springframework.stereotype.Repository
public interface PriceAnalysisRepository extends Repository<VLvlengRixiaoshou, Long> {
    
    /**
     * 查询价格偏差
     */
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
    List<RawPriceDeviation> findPriceDiff(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 查询客户交易
     */
    @Query(value = """

            SELECT SUM(销量) AS volume,ROUND(SUM(金额*10000)/SUM(销量),2) AS price,客户名称 AS customer
            FROM v_sales_detail_all
            WHERE 过账日期 >= :beginDate AND 过账日期 <= :endDate AND 渠道 = :region AND 物料组= :code AND 物料组描述 <> '无价值物料' AND 销量 <> 0
            GROUP BY 客户名称
      """, nativeQuery = true)
    List<CustomerTransactionProjection> findCustomerTransaction(@Param("region") String region, @Param("code") String code, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
}
