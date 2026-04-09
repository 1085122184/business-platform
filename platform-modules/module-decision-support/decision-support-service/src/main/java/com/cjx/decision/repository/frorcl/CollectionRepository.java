package com.cjx.decision.repository.frorcl;

import com.cjx.decision.entity.frorcl.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * @author cuijixu
 */
@Repository
public interface CollectionRepository extends JpaRepository<VLvlengRixiaoshou, Long> {

    @Query(value = """
      SELECT
                CASE 公司编码 WHEN '1200' THEN '高分子'
                WHEN '1300' THEN '氟硅'
                WHEN '3000' THEN '绿冷'
                WHEN '1400' THEN '有机硅'
                END AS companyName,
                SUM(回款) AS value,
                SN,会计期间 AS yesterday
                FROM V_HUIKUAN_ALL
            WHERE 会计期间 = :targetDate GROUP BY 公司编码,SN,会计期间 ORDER BY SN
      """, nativeQuery = true)
    List<CollectionDetail> findCollectionCompanies(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT 
      COMPANY AS companyName,PLANACCOUNTS AS planValue,CHECKYEAR ||'-'|| CHECKMONTH AS yesterday  
      FROM SO_PLANACCOUNT
      WHERE CHECKYEAR ||'-'|| CHECKMONTH =  :targetDate
      """, nativeQuery = true)
    List<CollectionPlan> findCollectionPlan(@Param("targetDate") String targetDate);
}
