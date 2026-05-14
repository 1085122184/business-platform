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
}
