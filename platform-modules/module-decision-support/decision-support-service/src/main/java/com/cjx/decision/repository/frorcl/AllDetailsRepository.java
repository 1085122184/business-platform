package com.cjx.decision.repository.frorcl;

import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import com.cjx.decision.projection.frorcl.AllDetails;
import com.cjx.decision.projection.frorcl.OrderDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 只读Repository，用于查询订单相关数据。
 * 仅提供查询方法，不支持增删改操作。
 *
 * @author cuijixu
 */
@org.springframework.stereotype.Repository
public interface AllDetailsRepository extends Repository<VLvlengRixiaoshou, Long> {

    @Query(value = """
  SELECT 过账日期 businessDate,
  CASE 工厂 WHEN '1201' THEN '高分子' WHEN '1301' THEN '氟硅'
          WHEN '3001' THEN '绿冷' WHEN '1400' THEN '有机硅'
          END AS companyName,
  CASE 渠道 WHEN '10' THEN '国内' WHEN '20' THEN '国外' END AS region,
  物料描述 AS productName,物料组描述 AS groupName,销量 AS sales,金额 AS amount,ROUND(金额*10000/销量,2) AS price
  FROM v_sales_detail_all WHERE 过账日期 = :targetDate AND  (:companyCode IS NULL OR :companyCode = '' OR 公司编码 = :companyCode)
  """, nativeQuery = true)
    List<AllDetails> findSalesDetail(@Param("targetDate") String targetDate,@Param("companyCode") String companyCode);
}
