package com.cjx.decision.repository.frorcl;

import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Repository
public interface ProductionRepository extends Repository<VLvlengRixiaoshou, Long> {

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(NVL(产量, 0)), 0), 2) AS TOTAL_OUTPUT,
          COUNT(DISTINCT 物料组) AS MATERIAL_GROUP_COUNT,
          COUNT(DISTINCT 工厂) AS FACTORY_COUNT
      FROM dwm_v_jt_chanliang
      WHERE 过账日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getOutputOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(NVL(数量, 0)), 0), 2) AS TOTAL_CONSUMPTION,
          COUNT(DISTINCT 物料描述) AS MATERIAL_COUNT,
          COUNT(DISTINCT 工厂) AS FACTORY_COUNT
      FROM dwm_v_jt_yuannliaoxiaohao
      WHERE 过账日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getRawConsumptionOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(NVL("SUM(库存)", 0)), 0), 2) AS PRODUCT_INVENTORY,
          COUNT(DISTINCT 物料组描述) AS MATERIAL_GROUP_COUNT,
          COUNT(DISTINCT 工厂) AS FACTORY_COUNT,
          MAX(更新时间) AS LATEST_UPDATE_TIME
      FROM dwm_v_jt_kucun_chanpin
      WHERE 更新时间 = (SELECT MAX(更新时间) FROM dwm_v_jt_kucun_chanpin)
      """, nativeQuery = true)
    Map<String, Object> getProductInventoryOverview();

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(NVL(吞量, 0)), 0), 2) AS INBOUND,
          ROUND(NVL(SUM(NVL(吐量, 0)), 0), 2) AS OUTBOUND,
          ROUND(NVL(SUM(NVL(车辆数, 0)), 0), 2) AS VEHICLE_COUNT,
          COUNT(DISTINCT 工厂) AS FACTORY_COUNT
      FROM dwm_v_jt_tuntuliang
      WHERE 过账日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getThroughputOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(NVL(水, 0)), 0), 2) AS WATER,
          MAX(水单位) AS WATER_UNIT,
          ROUND(NVL(SUM(NVL(电, 0)), 0), 2) AS ELECTRICITY,
          MAX(电单位) AS ELECTRICITY_UNIT,
          ROUND(NVL(SUM(NVL(制冷, 0)), 0), 2) AS REFRIGERATION,
          MAX(制冷单位) AS REFRIGERATION_UNIT,
          ROUND(NVL(SUM(NVL(蒸汽, 0)), 0), 2) AS STEAM,
          MAX(蒸汽单位) AS STEAM_UNIT,
          ROUND(NVL(SUM(NVL(天然气, 0)), 0), 2) AS NATURAL_GAS,
          MAX(天然气单位) AS NATURAL_GAS_UNIT,
          ROUND(NVL(SUM(NVL(氢气, 0)), 0), 2) AS HYDROGEN,
          MAX(氢气单位) AS HYDROGEN_UNIT,
          ROUND(NVL(SUM(NVL(纯水, 0)), 0), 2) AS PURE_WATER,
          MAX(纯水单位) AS PURE_WATER_UNIT
      FROM dwm_v_jt_nengyuan_mx
      WHERE 过账日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getEnergyOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(CASE WHEN 数据 LIKE '%开%' THEN NVL(数目, 0) ELSE 0 END), 0), 2) AS STARTUP_COUNT,
          ROUND(NVL(SUM(CASE WHEN 数据 LIKE '%停%' THEN NVL(数目, 0) ELSE 0 END), 0), 2) AS SHUTDOWN_COUNT,
          ROUND(NVL(SUM(NVL(数目, 0)), 0), 2) AS TOTAL_COUNT
      FROM dwm_v_jt_kaitingche
      """, nativeQuery = true)
    Map<String, Object> getStartupShutdownOverview();

    @Query(value = """
      SELECT
          ROUND(NVL(SUM(NVL(ALLRISKCOUNT, 0)), 0), 2) AS RISK_COUNT,
          ROUND(NVL(SUM(NVL(DEALCOUNT, 0)), 0), 2) AS DEAL_COUNT
      FROM DWM_V_JT_AQ_ZHENGGAI
      WHERE YEAR = :targetYear
      """, nativeQuery = true)
    Map<String, Object> getSafetyRectificationOverview(@Param("targetYear") String targetYear);

    @Query(value = """
      SELECT ROUND(NVL(SUM(NVL(作业数, 0)), 0), 2) AS WORK_COUNT
      FROM DWM_V_JT_AQ_GAOWEI
      WHERE 作业日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getHighRiskWorkOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT ROUND(NVL(SUM(NVL(报警数, 0)), 0), 2) AS ALARM_COUNT
      FROM DWM_V_JT_AQ_YOUDU
      WHERE 报警日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getToxicGasOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT ROUND(NVL(SUM(NVL(产生量, 0)), 0), 2) AS WASTE_OUTPUT
      FROM DWM_V_JT_WF_CHANSHENG
      WHERE 过账日期 = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getWasteOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          COUNT(DISTINCT CASE WHEN C0008_ITEM_DESC LIKE '%废气%' THEN C0007_SUBNAME END) AS EXHAUST_POINTS,
          COUNT(DISTINCT CASE WHEN C0008_ITEM_DESC LIKE '%废水%' THEN C0007_SUBNAME END) AS WASTEWATER_POINTS,
          COUNT(DISTINCT C0007_SUBNAME) AS TOTAL_POINTS
      FROM DWM_V_JT_HUBAO_FEISHUIQI
      WHERE SUBSTR(GENERATE_TIME, 1, 10) = :targetDate
      """, nativeQuery = true)
    Map<String, Object> getWaterGasOverview(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          物料组 AS CODE,
          NVL(物料组描述, '未分类') AS NAME,
          ROUND(NVL(SUM(NVL(产量, 0)), 0), 2) AS TOTAL_VALUE,
          COUNT(DISTINCT 工厂) AS FACTORY_COUNT
      FROM dwm_v_jt_chanliang
      WHERE 过账日期 = :targetDate
      GROUP BY 物料组, 物料组描述
      ORDER BY TOTAL_VALUE DESC
      """, nativeQuery = true)
    List<Map<String, Object>> getOutputByMaterialGroup(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          NVL(物料描述, '未命名物料') AS NAME,
          NVL(单位描述, '') AS UNIT,
          ROUND(NVL(SUM(NVL(数量, 0)), 0), 2) AS TOTAL_VALUE,
          COUNT(DISTINCT 工厂) AS FACTORY_COUNT
      FROM dwm_v_jt_yuannliaoxiaohao
      WHERE 过账日期 = :targetDate
      GROUP BY 物料描述, 单位描述
      ORDER BY TOTAL_VALUE DESC
      """, nativeQuery = true)
    List<Map<String, Object>> getRawConsumptionByMaterial(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT
          工厂 AS FACTORY,
          NVL(物料组描述, '未分类') AS NAME,
          ROUND(NVL(SUM(NVL("SUM(库存)", 0)), 0), 2) AS TOTAL_VALUE,
          MAX(更新时间) AS UPDATE_TIME
      FROM dwm_v_jt_kucun_chanpin
      WHERE 更新时间 = (SELECT MAX(更新时间) FROM dwm_v_jt_kucun_chanpin)
      GROUP BY 工厂, 物料组描述
      ORDER BY TOTAL_VALUE DESC
      """, nativeQuery = true)
    List<Map<String, Object>> getProductInventory();

    @Query(value = """
      SELECT
          工厂 AS FACTORY,
          公司 AS COMPANY,
          分类 AS CATEGORY,
          ROUND(NVL(SUM(NVL(吞量, 0)), 0), 2) AS INBOUND,
          ROUND(NVL(SUM(NVL(吐量, 0)), 0), 2) AS OUTBOUND,
          ROUND(NVL(SUM(NVL(车辆数, 0)), 0), 2) AS VEHICLE_COUNT
      FROM dwm_v_jt_tuntuliang
      WHERE 过账日期 = :targetDate
      GROUP BY 工厂, 公司, 分类
      ORDER BY 工厂, 分类
      """, nativeQuery = true)
    List<Map<String, Object>> getThroughputByFactory(@Param("targetDate") String targetDate);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  公司 AS COMPANY,
                  物料 AS MATERIAL_CODE,
                  物料描述 AS MATERIAL_NAME,
                  物料组 AS MATERIAL_GROUP,
                  物料组描述 AS MATERIAL_GROUP_NAME,
                  月份 AS MONTH_VALUE,
                  过账日期 AS POSTING_DATE,
                  工作中心 AS WORK_CENTER,
                  ROUND(NVL(产量, 0), 2) AS OUTPUT_VALUE
              FROM dwm_v_jt_chanliang_mingxi
              WHERE 过账日期 = :targetDate
                AND (:company IS NULL OR :company = '' OR 公司 = :company)
                AND (:materialGroup IS NULL OR :materialGroup = '' OR 物料组 = :materialGroup OR 物料组描述 = :materialGroup)
                AND (:keyword IS NULL OR :keyword = '' OR 物料 LIKE '%' || :keyword || '%' OR 物料描述 LIKE '%' || :keyword || '%' OR 工作中心 LIKE '%' || :keyword || '%')
              ORDER BY OUTPUT_VALUE DESC
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getOutputDetails(
            @Param("targetDate") String targetDate,
            @Param("company") String company,
            @Param("materialGroup") String materialGroup,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM dwm_v_jt_chanliang_mingxi
      WHERE 过账日期 = :targetDate
        AND (:company IS NULL OR :company = '' OR 公司 = :company)
        AND (:materialGroup IS NULL OR :materialGroup = '' OR 物料组 = :materialGroup OR 物料组描述 = :materialGroup)
        AND (:keyword IS NULL OR :keyword = '' OR 物料 LIKE '%' || :keyword || '%' OR 物料描述 LIKE '%' || :keyword || '%' OR 工作中心 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countOutputDetails(
            @Param("targetDate") String targetDate,
            @Param("company") String company,
            @Param("materialGroup") String materialGroup,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  输入日期 AS INPUT_DATE,
                  过账日期 AS POSTING_DATE,
                  工厂 AS FACTORY,
                  工厂名称 AS FACTORY_NAME,
                  物料 AS MATERIAL_CODE,
                  物料描述 AS MATERIAL_NAME,
                  物料组 AS MATERIAL_GROUP,
                  物料组名称 AS MATERIAL_GROUP_NAME,
                  库存地点描述 AS STORAGE_LOCATION,
                  单位描述 AS UNIT_VALUE,
                  工作中心 AS WORK_CENTER,
                  工作中心描述 AS WORK_CENTER_NAME,
                  订单 AS ORDER_NO,
                  批次 AS BATCH_NO,
                  ROUND(NVL(数量, 0), 2) AS QUANTITY,
                  生产计划 AS PRODUCTION_PLAN,
                  物料凭证 AS MATERIAL_DOCUMENT,
                  移动类型 AS MOVEMENT_TYPE,
                  移动类型名称 AS MOVEMENT_TYPE_NAME,
                  月份 AS MONTH_VALUE
              FROM dwm_v_jt_yuannliaoxiaohao_mx
              WHERE 过账日期 = :targetDate
                AND (:factory IS NULL OR :factory = '' OR 工厂 = :factory OR 工厂名称 = :factory)
                AND (:material IS NULL OR :material = '' OR 物料 = :material OR 物料描述 = :material)
                AND (:keyword IS NULL OR :keyword = '' OR 物料 LIKE '%' || :keyword || '%' OR 物料描述 LIKE '%' || :keyword || '%' OR 订单 LIKE '%' || :keyword || '%' OR 批次 LIKE '%' || :keyword || '%')
              ORDER BY QUANTITY DESC
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getRawConsumptionDetails(
            @Param("targetDate") String targetDate,
            @Param("factory") String factory,
            @Param("material") String material,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM dwm_v_jt_yuannliaoxiaohao_mx
      WHERE 过账日期 = :targetDate
        AND (:factory IS NULL OR :factory = '' OR 工厂 = :factory OR 工厂名称 = :factory)
        AND (:material IS NULL OR :material = '' OR 物料 = :material OR 物料描述 = :material)
        AND (:keyword IS NULL OR :keyword = '' OR 物料 LIKE '%' || :keyword || '%' OR 物料描述 LIKE '%' || :keyword || '%' OR 订单 LIKE '%' || :keyword || '%' OR 批次 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countRawConsumptionDetails(
            @Param("targetDate") String targetDate,
            @Param("factory") String factory,
            @Param("material") String material,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  订单编号 AS ORDER_NO,
                  业务号 AS BUSINESS_NO,
                  业务流程类型 AS PROCESS_TYPE,
                  车牌号 AS PLATE_NO,
                  司机 AS DRIVER,
                  司机电话 AS DRIVER_PHONE,
                  ROUND(NVL(组毛, 0), 2) AS GROSS_WEIGHT,
                  ROUND(NVL(组皮, 0), 2) AS TARE_WEIGHT,
                  ROUND(NVL(组净, 0), 2) AS NET_WEIGHT,
                  物料名称 AS MATERIAL_NAME,
                  过磅日期 AS WEIGHING_DATE,
                  客户名称 AS CUSTOMER_NAME,
                  承运商 AS CARRIER,
                  单位名称 AS COMPANY_NAME,
                  分类 AS CATEGORY
              FROM dwm_v_jt_tuntuliang_mingxi
              WHERE 过磅日期 = :targetDate
                AND (:category IS NULL OR :category = '' OR 分类 = :category)
                AND (:keyword IS NULL OR :keyword = '' OR 订单编号 LIKE '%' || :keyword || '%' OR 业务号 LIKE '%' || :keyword || '%' OR 车牌号 LIKE '%' || :keyword || '%' OR 物料名称 LIKE '%' || :keyword || '%' OR 客户名称 LIKE '%' || :keyword || '%')
              ORDER BY NET_WEIGHT DESC
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getTransportDetails(
            @Param("targetDate") String targetDate,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM dwm_v_jt_tuntuliang_mingxi
      WHERE 过磅日期 = :targetDate
        AND (:category IS NULL OR :category = '' OR 分类 = :category)
        AND (:keyword IS NULL OR :keyword = '' OR 订单编号 LIKE '%' || :keyword || '%' OR 业务号 LIKE '%' || :keyword || '%' OR 车牌号 LIKE '%' || :keyword || '%' OR 物料名称 LIKE '%' || :keyword || '%' OR 客户名称 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countTransportDetails(
            @Param("targetDate") String targetDate,
            @Param("category") String category,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  输入日期 AS INPUT_DATE,
                  过账日期 AS POSTING_DATE,
                  订单 AS ORDER_NO,
                  工厂 AS FACTORY,
                  ROUND(NVL(水, 0), 2) AS WATER,
                  水单位 AS WATER_UNIT,
                  ROUND(NVL(电, 0), 2) AS ELECTRICITY,
                  电单位 AS ELECTRICITY_UNIT,
                  ROUND(NVL(制冷, 0), 2) AS REFRIGERATION,
                  制冷单位 AS REFRIGERATION_UNIT,
                  ROUND(NVL(蒸汽, 0), 2) AS STEAM,
                  蒸汽单位 AS STEAM_UNIT,
                  ROUND(NVL(天然气, 0), 2) AS NATURAL_GAS,
                  天然气单位 AS NATURAL_GAS_UNIT,
                  ROUND(NVL(氢气, 0), 2) AS HYDROGEN,
                  氢气单位 AS HYDROGEN_UNIT,
                  ROUND(NVL(纯水, 0), 2) AS PURE_WATER,
                  纯水单位 AS PURE_WATER_UNIT
              FROM dwm_v_jt_nengyuan_mx
              WHERE 过账日期 = :targetDate
                AND (:keyword IS NULL OR :keyword = '' OR 订单 LIKE '%' || :keyword || '%' OR 工厂 LIKE '%' || :keyword || '%')
              ORDER BY 工厂, 订单
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getEnergyDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM dwm_v_jt_nengyuan_mx
      WHERE 过账日期 = :targetDate
        AND (:keyword IS NULL OR :keyword = '' OR 订单 LIKE '%' || :keyword || '%' OR 工厂 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countEnergyDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  RN AS SOURCE_RN,
                  公司 AS COMPANY,
                  装置 AS DEVICE,
                  时间 AS TIME_VALUE,
                  DEV AS DEV_VALUE,
                  BIAOZHUN AS STANDARD_VALUE,
                  排序 AS SORT_VALUE,
                  值 AS VALUE_TEXT
              FROM dwm_v_jt_kaitingche_mx
              WHERE TO_CHAR(时间, 'YYYY-MM-DD') = :targetDate
                AND (:keyword IS NULL OR :keyword = '' OR 公司 LIKE '%' || :keyword || '%' OR 装置 LIKE '%' || :keyword || '%' OR DEV LIKE '%' || :keyword || '%' OR BIAOZHUN LIKE '%' || :keyword || '%' OR 值 LIKE '%' || :keyword || '%')
              ORDER BY 排序, RN
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getStartupShutdownDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM dwm_v_jt_kaitingche_mx
      WHERE TO_CHAR(时间, 'YYYY-MM-DD') = :targetDate
        AND (:keyword IS NULL OR :keyword = '' OR 公司 LIKE '%' || :keyword || '%' OR 装置 LIKE '%' || :keyword || '%' OR DEV LIKE '%' || :keyword || '%' OR BIAOZHUN LIKE '%' || :keyword || '%' OR 值 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countStartupShutdownDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  作业日期 AS WORK_DATE,
                  公司名称 AS COMPANY_NAME,
                  ROUND(NVL(作业数, 0), 2) AS WORK_COUNT
              FROM DWM_V_JT_AQ_GAOWEI
              WHERE 作业日期 = :targetDate
                AND (:keyword IS NULL OR :keyword = '' OR 公司名称 LIKE '%' || :keyword || '%')
              ORDER BY WORK_COUNT DESC, 公司名称
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getHighRiskWorkDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM DWM_V_JT_AQ_GAOWEI
      WHERE 作业日期 = :targetDate
        AND (:keyword IS NULL OR :keyword = '' OR 公司名称 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countHighRiskWorkDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  报警日期 AS ALARM_DATE,
                  公司 AS COMPANY,
                  ROUND(NVL(报警数, 0), 2) AS ALARM_COUNT
              FROM DWM_V_JT_AQ_YOUDU
              WHERE 报警日期 = :targetDate
                AND (:keyword IS NULL OR :keyword = '' OR 公司 LIKE '%' || :keyword || '%')
              ORDER BY ALARM_COUNT DESC, 公司
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getToxicGasDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM DWM_V_JT_AQ_YOUDU
      WHERE 报警日期 = :targetDate
        AND (:keyword IS NULL OR :keyword = '' OR 公司 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countToxicGasDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  过账日期 AS POSTING_DATE,
                  公司编码 AS COMPANY_CODE,
                  公司名称 AS COMPANY_NAME,
                  公司 AS COMPANY,
                  危废代码 AS WASTE_CODE,
                  危废名称 AS WASTE_NAME,
                  ROUND(NVL(产生量, 0), 2) AS OUTPUT_VALUE
              FROM DWM_V_JT_WF_CHANSHENG_MX
              WHERE 过账日期 = :targetDate
                AND (:keyword IS NULL OR :keyword = '' OR 公司编码 LIKE '%' || :keyword || '%' OR 公司名称 LIKE '%' || :keyword || '%' OR 公司 LIKE '%' || :keyword || '%' OR 危废代码 LIKE '%' || :keyword || '%' OR 危废名称 LIKE '%' || :keyword || '%')
              ORDER BY OUTPUT_VALUE DESC, 公司名称, 危废名称
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getWasteDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM DWM_V_JT_WF_CHANSHENG_MX
      WHERE 过账日期 = :targetDate
        AND (:keyword IS NULL OR :keyword = '' OR 公司编码 LIKE '%' || :keyword || '%' OR 公司名称 LIKE '%' || :keyword || '%' OR 公司 LIKE '%' || :keyword || '%' OR 危废代码 LIKE '%' || :keyword || '%' OR 危废名称 LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countWasteDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword);

    @Query(value = """
      SELECT *
      FROM (
          SELECT t.*, ROWNUM rn
          FROM (
              SELECT
                  C0007_PCODE_C AS POINT_CODE,
                  C0007_PNAME AS POINT_NAME,
                  C0007_SUBNAME AS SUB_NAME,
                  C0008_ITEM_DESC AS ITEM_DESC,
                  GENERATE_TIME AS GENERATE_TIME_VALUE,
                  DATETIME_JT AS GROUP_TIME
              FROM DWM_V_JT_HUBAO_FEISHUIQI
              WHERE SUBSTR(GENERATE_TIME, 1, 10) = :targetDate
                AND (:keyword IS NULL OR :keyword = '' OR C0007_PCODE_C LIKE '%' || :keyword || '%' OR C0007_PNAME LIKE '%' || :keyword || '%' OR C0007_SUBNAME LIKE '%' || :keyword || '%' OR C0008_ITEM_DESC LIKE '%' || :keyword || '%')
              ORDER BY GENERATE_TIME DESC, C0007_PNAME, C0007_SUBNAME
          ) t
          WHERE ROWNUM <= :endRow
      )
      WHERE rn > :startRow
      """, nativeQuery = true)
    List<Map<String, Object>> getWaterGasDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow);

    @Query(value = """
      SELECT COUNT(1)
      FROM DWM_V_JT_HUBAO_FEISHUIQI
      WHERE SUBSTR(GENERATE_TIME, 1, 10) = :targetDate
        AND (:keyword IS NULL OR :keyword = '' OR C0007_PCODE_C LIKE '%' || :keyword || '%' OR C0007_PNAME LIKE '%' || :keyword || '%' OR C0007_SUBNAME LIKE '%' || :keyword || '%' OR C0008_ITEM_DESC LIKE '%' || :keyword || '%')
      """, nativeQuery = true)
    long countWaterGasDetails(
            @Param("targetDate") String targetDate,
            @Param("keyword") String keyword);
}
