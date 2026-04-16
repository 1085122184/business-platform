package com.cjx.decision.repository.frorcl;

import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Repository
public interface ExpenseRepository extends Repository<VLvlengRixiaoshou, String> {

    // 1. 总览数据查询
    @Query(value = "SELECT " +
            "SUM(CASE WHEN BIZ_DATE = :curr THEN SALES_EXP ELSE 0 END) as CUR_SALES, " +
            "SUM(CASE WHEN BIZ_DATE = :last THEN SALES_EXP ELSE 0 END) as LST_SALES, " +
            "SUM(CASE WHEN BIZ_DATE = :curr THEN MANAGE_EXP ELSE 0 END) as CUR_MANAGE, " +
            "SUM(CASE WHEN BIZ_DATE = :last THEN MANAGE_EXP ELSE 0 END) as LST_MANAGE, " +
            "SUM(CASE WHEN BIZ_DATE = :curr THEN FINANCE_EXP ELSE 0 END) as CUR_FINANCE, " +
            "SUM(CASE WHEN BIZ_DATE = :last THEN FINANCE_EXP ELSE 0 END) as LST_FINANCE " +
            "FROM T_BI_EXPENSE_REPORT " +
            "WHERE BIZ_DATE IN (:curr, :last)", nativeQuery = true)
    Map<String, Object> getComparisonData(String curr, String last);

    // 2. 趋势数据查询 (按月汇总)
    @Query(value = "SELECT TO_CHAR(TO_DATE(BIZ_DATE, 'YYYY-MM-DD'), 'YYYY-MM') as MONTH_STR, " +
            "SUM(SALES_EXP) as SALES, SUM(MANAGE_EXP) as MANAGE, SUM(FINANCE_EXP) as FINANCE " +
            "FROM T_BI_EXPENSE_REPORT " +
            "WHERE BIZ_DATE BETWEEN :startDate AND :endDate " +
            "GROUP BY TO_CHAR(TO_DATE(BIZ_DATE, 'YYYY-MM-DD'), 'YYYY-MM') " +
            "ORDER BY MONTH_STR ASC", nativeQuery = true)
    List<Map<String, Object>> getMonthlyTrend(String startDate, String endDate);

    // 3. 各公司明细分页查询 (支持关键字筛选)
    @Query(value = "SELECT COMPANY_NAME, SALES_EXP, MANAGE_EXP, FINANCE_EXP, TOTAL_EXP, YOY_RATE " +
            "FROM (SELECT t.*, ROWNUM rn FROM (" +
            "      SELECT COMPANY_NAME, SUM(SALES_EXP) as SALES_EXP, SUM(MANAGE_EXP) as MANAGE_EXP, " +
            "             SUM(FINANCE_EXP) as FINANCE_EXP, SUM(SALES_EXP+MANAGE_EXP+FINANCE_EXP) as TOTAL_EXP, " +
            "             AVG(YOY_RATE) as YOY_RATE " +
            "      FROM T_BI_EXPENSE_REPORT " +
            "      WHERE BIZ_DATE = :date AND (:keyword IS NULL OR COMPANY_NAME LIKE '%' || :keyword || '%') " +
            "      GROUP BY COMPANY_NAME ORDER BY TOTAL_EXP DESC) t " +
            ") WHERE rn > :offset AND rn <= (:offset + :pageSize)", nativeQuery = true)
    List<Map<String, Object>> getCompanyDetailsByPage(String date, String keyword, int offset, int pageSize);

    // 4. 各公司明细总数 (用于分页)
    @Query(value = "SELECT COUNT(DISTINCT COMPANY_NAME) FROM T_BI_EXPENSE_REPORT " +
            "WHERE BIZ_DATE = :date AND (:keyword IS NULL OR COMPANY_NAME LIKE '%' || :keyword || '%')", nativeQuery = true)
    long countCompanyDetails(String date, String keyword);

    // 5. 各公司对比查询 (不分页，取 Top 10)
    @Query(value = "SELECT COMPANY_NAME, SUM(SALES_EXP) as SALES, SUM(MANAGE_EXP) as MANAGE, SUM(FINANCE_EXP) as FINANCE " +
            "FROM T_BI_EXPENSE_REPORT WHERE BIZ_DATE = :date " +
            "GROUP BY COMPANY_NAME ORDER BY SUM(SALES_EXP+MANAGE_EXP+FINANCE_EXP) DESC FETCH FIRST 10 ROWS ONLY", nativeQuery = true)
    List<Map<String, Object>> getCompanyComparison(String date);
}