package com.cjx.decision.repository.frorcl;

import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Repository
public interface ExpenseRepository extends Repository<VLvlengRixiaoshou, String> {

    // 1. 总览数据查询
    @Query(value = "SELECT " +
            "SUM(CASE WHEN 月份 = :curr AND 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) as CUR_SALES, " +
            "SUM(CASE WHEN 月份 = :last AND 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) as LST_SALES," +
            "SUM(CASE WHEN 月份 = :curr AND 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) as CUR_MANAGE," +
            "SUM(CASE WHEN 月份 = :last AND 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) as LST_MANAGE," +
            "SUM(CASE WHEN 月份 = :curr AND 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) as CUR_FINANCE, " +
            "SUM(CASE WHEN 月份 = :last AND 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) as LST_FINANCE " +
            "FROM dwm_v_jt_sanfei " +
            "WHERE 月份 IN (:curr, :last)", nativeQuery = true)
    Map<String, Object> getComparisonData(String curr, String last);

    // 2. 趋势数据查询 (按月汇总)
    @Query(value = "SELECT 月份 as MONTH_STR, " +
            " SUM(CASE WHEN 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) as SALES, SUM(CASE WHEN 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) as MANAGE, SUM(CASE WHEN 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) as FINANCE " +
            "FROM dwm_v_jt_sanfei " +
            "WHERE 月份 BETWEEN :startDate AND :endDate " +
            "GROUP BY 月份 " +
            "ORDER BY MONTH_STR ASC", nativeQuery = true)
    List<Map<String, Object>> getMonthlyTrend(String startDate, String endDate);

    // 3. 各公司明细分页查询 (支持关键字筛选)
    @Query(value = "SELECT COMPANY_NAME, SALES_EXP, MANAGE_EXP, FINANCE_EXP " +
            "FROM (SELECT t.*, ROWNUM rn FROM (" +
            "      SELECT 公司名称 AS COMPANY_NAME, " +
            "SUM(CASE WHEN 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) as SALES_EXP," +
            "SUM(CASE WHEN 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) as MANAGE_EXP, " +
            "SUM(CASE WHEN 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) as FINANCE_EXP " +
            "      FROM dwm_v_jt_sanfei " +
            "      WHERE 月份 = :date AND (:keyword IS NULL OR 公司名称 LIKE '%' || :keyword || '%') " +
            "      GROUP BY 公司名称 ) t " +
            ") WHERE rn >= :offset AND rn <= (:offset + :pageSize)", nativeQuery = true)
    List<Map<String, Object>> getCompanyDetailsByPage(String date, String keyword, int offset, int pageSize);

    // 4. 各公司明细总数 (用于分页)
    @Query(value = "SELECT COUNT(DISTINCT 公司名称) FROM dwm_v_jt_sanfei " +
            "WHERE 月份 = :date AND (:keyword IS NULL OR 公司名称 LIKE '%' || :keyword || '%')", nativeQuery = true)
    long countCompanyDetails(String date, String keyword);

    // 5. 各公司对比查询
    @Query(value = " SELECT 公司名称 AS COMPANY_NAME, " +
            "SUM(CASE WHEN 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) as SALES, " +
            "SUM(CASE WHEN 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) as MANAGE, " +
            "SUM(CASE WHEN 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) as FINANCE " +
            "FROM dwm_v_jt_sanfei WHERE 月份 BETWEEN :startDay AND :thisDay  " +
            "GROUP BY 公司名称 ORDER BY SUM(期末余额) DESC", nativeQuery = true)
    List<Map<String, Object>> getCompanyComparison(String startDay, String thisDay);

    @Query(value = "SELECT 公司名称 COMPANY_NAME,费用类型 TYPES,本币金额*10000 AMOUNT,行项目文本 TEXT " +
            "FROM dwm_v_jt_sanfei_mingxi " +
            "WHERE 过账日期 = :date AND 公司名称 = :companyName ", nativeQuery = true)
    List<Map<String, Object>> getDailyDetail(String date, String companyName);

    @Query(value = "  SELECT 公司 AS COMPANY_NAME," +
            "SUM(销售费用) AS SALES_BUDGET,SUM(管理费用) AS MANAGE_BUDGET,SUM(财务费用) AS FINANCE_BUDGET " +
            "FROM YS_FEIYONG_JT " +
            "WHERE 月份 BETWEEN :startDay AND :thisDay GROUP BY 公司 ", nativeQuery = true)
    List<Map<String, Object>> getBudget(String startDay,String thisDay);



    @Query(value = "SELECT 公司名称 as companyName, SUM(期末余额) as totalAmount " +
            "FROM dwm_v_jt_sanfei " +
            "WHERE 月份 = :month " +
            "GROUP BY 公司名称", nativeQuery = true)
    List<Map<String, Object>> findCompanyMonthlySums(String month);


    @Query(value = "SELECT 项目 as expenseType, SUM(期末余额) as totalAmount " +
            "FROM dwm_v_jt_sanfei " +
            "WHERE 月份 = :month " +
            "GROUP BY 项目", nativeQuery = true)
    List<Map<String, Object>> findExpenseOverviewByMonth(String month);

}