package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 趋势分析Repository
 * 负责查询销售趋势和产品深度数据
 * 
 * @author system
 * @version 1.0.0
 */
@org.springframework.stereotype.Repository
public interface TrendAnalysisRepository extends Repository<VLvlengRixiaoshou, Long> {
    
    /**
     * 查询月度趋势
     */
    @Query(value = """
       SELECT 公司编码,渠道 AS region,
                  物料组 AS productCode,物料组描述 AS productName,TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM') AS latestDate,
                  ROUND(SUM(NVL(开票量, 0)), 2) AS totalSales,
                  ROUND(SUM(NVL(开票金额, 0)), 2)  AS totalAmount,
                  ROUND(CASE WHEN SUM(NVL(开票量, 0)) = 0 THEN 0 ELSE SUM(NVL(开票金额, 0)) / SUM(NVL(开票量, 0))*10000 END,2) AS price
                  FROM v_dwm_jt_fapiao_mingxi_std
                  WHERE 物料组 = :productCode AND 渠道 = :region AND 物料组描述 <> '无价值物料' AND 日期 BETWEEN :startDate AND :endDate
                  GROUP BY 公司编码,渠道,物料组,物料组描述,TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM') ORDER BY TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM')
      """, nativeQuery = true)
    List<SalesSummary> findTrendsYear(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("productCode") String productCode, @Param("region") String region);
    
    /**
     * 查询产品月度深度
     */
    @Query(value = """
      SELECT 渠道 AS region,物料组 AS productCode,物料组描述 AS productName,SUM(销量) AS totalSales,SUM(金额) AS totalAmount,过账日期 AS latestDate FROM v_sales_detail_all
      WHERE 公司编码 = :companyName AND 物料组 = :productCode
      AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate-1, 'MM')
      AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate-1)
      GROUP BY 渠道,物料组,物料组描述,过账日期
      ORDER BY 过账日期
      """, nativeQuery = true)
    List<SalesSummary> getProductDeepMonth(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);
    
    /**
     * 查询产品年度深度
     */
    @Query(value = """
      SELECT CASE WHEN 渠道 = '国内' THEN '10' WHEN 渠道 = '国外' THEN '20' ELSE '30' END AS region,物料组 AS productCode,物料组描述 AS productName,SUM(开票量) AS totalSales,SUM(开票金额) AS totalAmount,SUBSTR(日期,1,7)  AS latestDate FROM v_dwm_jt_fapiao_mingxi_std
                  WHERE 公司编码 = :companyName AND 物料组  = :productCode AND 渠道 <> '关联'
                  AND SUBSTR(日期,1,4) = TO_CHAR(TRUNC(:targetDate-1), 'YYYY')
                  GROUP BY 渠道,物料组,物料组描述,SUBSTR(日期,1,7)
                  ORDER BY SUBSTR(日期,1,7)
      """, nativeQuery = true)
    List<SalesSummary> getProductDeepYear(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);
    
    /**
     * 查询本年，客户购买量
     */
    @Query(value = """
      SELECT 客户名称 AS customer,渠道 AS region,物料组 AS productCode,物料组描述 AS productName,SUM(开票量) AS totalSales,SUM(开票金额) AS totalAmount FROM v_dwm_jt_fapiao_mingxi_std
                  WHERE 公司编码 = :companyName AND 物料组 = :productCode AND 渠道 <> '关联'
                  AND SUBSTR(日期,1,4) = TO_CHAR(TRUNC(:targetDate-1), 'YYYY')
                  GROUP BY 客户名称,渠道,物料组,物料组描述
                  ORDER BY totalSales DESC
      """, nativeQuery = true)
    List<SalesSummary> getProductCustomer(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);

    /**
     * 查询本月，客户购买量
     */
    @Query(value = """
      SELECT 客户名称 AS customer,渠道 AS region,物料组 AS productCode,物料组描述 AS productName,SUM(销量) AS totalSales,SUM(金额) AS totalAmount FROM v_sales_detail_all
                  WHERE 公司编码 = :companyName AND 物料组 = :productCode AND 渠道 <> '关联'
                  AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate-1, 'MM')
                  AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate-1)
                  GROUP BY 客户名称,渠道,物料组,物料组描述
                  ORDER BY totalSales DESC
      """, nativeQuery = true)
    List<SalesSummary> getProductCustomerMonth(@Param("companyName") String companyName, @Param("productCode") String productCode, @Param("targetDate") LocalDate targetDate);


    /**
     * 查询月度趋势(全量)
     */
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
    List<SalesSummary> findTrendsAll(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
