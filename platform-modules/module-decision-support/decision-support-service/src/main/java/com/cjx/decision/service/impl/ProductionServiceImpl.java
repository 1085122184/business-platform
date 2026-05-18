package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.annotation.AutoWarmUp;
import com.cjx.decision.dto.production.ProductionDetailPageDTO;
import com.cjx.decision.dto.production.ProductionEnergyDetailDTO;
import com.cjx.decision.dto.production.ProductionEnergyOverviewDTO;
import com.cjx.decision.dto.production.ProductionEnvironmentOverviewDTO;
import com.cjx.decision.dto.production.ProductionHighRiskWorkDetailDTO;
import com.cjx.decision.dto.production.ProductionMetricDTO;
import com.cjx.decision.dto.production.ProductionOverviewDTO;
import com.cjx.decision.dto.production.ProductionOutputDetailDTO;
import com.cjx.decision.dto.production.ProductionRankItemDTO;
import com.cjx.decision.dto.production.ProductionRawConsumptionDetailDTO;
import com.cjx.decision.dto.production.ProductionSafetyOverviewDTO;
import com.cjx.decision.dto.production.ProductionStartupShutdownDetailDTO;
import com.cjx.decision.dto.production.ProductionStartupShutdownOverviewDTO;
import com.cjx.decision.dto.production.ProductionToxicGasDetailDTO;
import com.cjx.decision.dto.production.ProductionThroughputDTO;
import com.cjx.decision.dto.production.ProductionTransportDetailDTO;
import com.cjx.decision.dto.production.ProductionWasteDetailDTO;
import com.cjx.decision.dto.production.ProductionWaterGasDetailDTO;
import com.cjx.decision.repository.frorcl.ProductionRepository;
import com.cjx.decision.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DEFAULT_TOP_LIMIT = 20;

    private final ProductionRepository productionRepository;
    private final CaffeineCacheService caffeineCacheService;

    @AutoWarmUp(order = 100)
    @Override
    public ProductionOverviewDTO getOverview(LocalDate date) {
        String key = "production:overview:" + date.format(DF);
        return caffeineCacheService.getOrLoad(
                CacheType.TODAY_DATA,
                key,
                k -> buildOverview(date),
                ProductionOverviewDTO.class
        );
    }

    private ProductionOverviewDTO buildOverview(LocalDate date) {
        String targetDate = date.format(DF);
        Map<String, Object> output = productionRepository.getOutputOverview(targetDate);
        Map<String, Object> raw = productionRepository.getRawConsumptionOverview(targetDate);
        Map<String, Object> inventory = productionRepository.getProductInventoryOverview();
        Map<String, Object> throughputRaw = productionRepository.getThroughputOverview(targetDate);
        Map<String, Object> energy = productionRepository.getEnergyOverview(targetDate);
        Map<String, Object> startupShutdownRaw = productionRepository.getStartupShutdownOverview();
        Map<String, Object> rectification = productionRepository.getSafetyRectificationOverview(String.valueOf(date.getYear()));
        Map<String, Object> highRisk = productionRepository.getHighRiskWorkOverview(targetDate);
        Map<String, Object> toxicGas = productionRepository.getToxicGasOverview(targetDate);
        Map<String, Object> waste = productionRepository.getWasteOverview(targetDate);
        Map<String, Object> waterGas = productionRepository.getWaterGasOverview(targetDate);

        ProductionOverviewDTO dto = new ProductionOverviewDTO();
        dto.setDate(targetDate);
        dto.setTotalOutput(new ProductionMetricDTO("总产量（日）", decimal(output, "TOTAL_OUTPUT"), "吨", "按物料组汇总"));
        dto.setRawMaterialConsumption(new ProductionMetricDTO("主原料总耗用（日）", decimal(raw, "TOTAL_CONSUMPTION"), "吨", "按原料消耗汇总"));
        dto.setProductInventory(new ProductionMetricDTO("主产品库存", decimal(inventory, "PRODUCT_INVENTORY"), "吨", "最新库存快照"));
        dto.setOutputMaterialGroups(new ProductionMetricDTO("生产物料组数", decimal(output, "MATERIAL_GROUP_COUNT"), "个", "当日产量覆盖"));
        dto.setRawMaterialKinds(new ProductionMetricDTO("原料品种数", decimal(raw, "MATERIAL_COUNT"), "个", "当日消耗覆盖"));
        dto.setLatestInventoryTime(stringValue(inventory, "LATEST_UPDATE_TIME"));

        ProductionThroughputDTO throughput = new ProductionThroughputDTO();
        throughput.setInbound(decimal(throughputRaw, "INBOUND"));
        throughput.setOutbound(decimal(throughputRaw, "OUTBOUND"));
        throughput.setVehicleCount(decimal(throughputRaw, "VEHICLE_COUNT"));
        throughput.setUnit("吨");
        dto.setThroughput(throughput);
        dto.setEnergy(buildEnergyOverview(energy));
        dto.setStartupShutdown(buildStartupShutdownOverview(startupShutdownRaw));
        dto.setSafety(buildSafetyOverview(rectification, highRisk, toxicGas));
        dto.setEnvironment(buildEnvironmentOverview(waste, waterGas));
        return dto;
    }

    private ProductionEnergyOverviewDTO buildEnergyOverview(Map<String, Object> row) {
        ProductionEnergyOverviewDTO dto = new ProductionEnergyOverviewDTO();
        dto.setWater(metric("水", decimal(row, "WATER"), stringValue(row, "WATER_UNIT"), "能源消耗"));
        dto.setElectricity(metric("电", decimal(row, "ELECTRICITY"), stringValue(row, "ELECTRICITY_UNIT"), "能源消耗"));
        dto.setRefrigeration(metric("制冷", decimal(row, "REFRIGERATION"), stringValue(row, "REFRIGERATION_UNIT"), "能源消耗"));
        dto.setSteam(metric("蒸汽", decimal(row, "STEAM"), stringValue(row, "STEAM_UNIT"), "能源消耗"));
        dto.setNaturalGas(metric("天然气", decimal(row, "NATURAL_GAS"), stringValue(row, "NATURAL_GAS_UNIT"), "能源消耗"));
        dto.setHydrogen(metric("氢气", decimal(row, "HYDROGEN"), stringValue(row, "HYDROGEN_UNIT"), "能源消耗"));
        dto.setPureWater(metric("纯水", decimal(row, "PURE_WATER"), stringValue(row, "PURE_WATER_UNIT"), "能源消耗"));
        return dto;
    }

    private ProductionStartupShutdownOverviewDTO buildStartupShutdownOverview(Map<String, Object> row) {
        ProductionStartupShutdownOverviewDTO dto = new ProductionStartupShutdownOverviewDTO();
        dto.setStartupCount(decimal(row, "STARTUP_COUNT"));
        dto.setShutdownCount(decimal(row, "SHUTDOWN_COUNT"));
        dto.setTotalCount(decimal(row, "TOTAL_COUNT"));
        dto.setUnit("次");
        return dto;
    }

    private ProductionSafetyOverviewDTO buildSafetyOverview(
            Map<String, Object> rectification, Map<String, Object> highRisk, Map<String, Object> toxicGas) {
        BigDecimal riskCount = decimal(rectification, "RISK_COUNT");
        BigDecimal dealCount = decimal(rectification, "DEAL_COUNT");
        ProductionSafetyOverviewDTO dto = new ProductionSafetyOverviewDTO();
        dto.setProcessAlarm(metric("工艺报警数", null, "次", "暂无数据源"));
        dto.setEquipmentAlarm(metric("设备报警数", null, "次", "暂无数据源"));
        dto.setHighRiskWork(metric("高危作业", decimal(highRisk, "WORK_COUNT"), "日", "高危作业数"));
        dto.setToxicGasAlarm(metric("可燃有毒报警数", decimal(toxicGas, "ALARM_COUNT"), "次", "有毒气体报警"));
        dto.setRiskCount(metric("隐患总数", riskCount, "项", "安全整改"));
        dto.setDealCount(metric("整改完成数", dealCount, "项", "安全整改"));
        dto.setRectificationRate(percent(dealCount, riskCount));
        return dto;
    }

    private ProductionEnvironmentOverviewDTO buildEnvironmentOverview(Map<String, Object> waste, Map<String, Object> waterGas) {
        ProductionEnvironmentOverviewDTO dto = new ProductionEnvironmentOverviewDTO();
        dto.setExhaustEmissionPoints(metric("废气异常排放点", decimal(waterGas, "EXHAUST_POINTS"), "个", "按子点位去重"));
        dto.setWastewaterEmissionPoints(metric("废水异常排放点", decimal(waterGas, "WASTEWATER_POINTS"), "个", "按子点位去重"));
        dto.setTotalWaterGasPoints(metric("废气废水异常点", decimal(waterGas, "TOTAL_POINTS"), "个", "按子点位去重"));
        dto.setHazardousWaste(metric("固废产生量（不含自行处置）", decimal(waste, "WASTE_OUTPUT"), "吨", "危废产生量"));
        return dto;
    }

    @AutoWarmUp(order = 110)
    @Override
    public List<ProductionRankItemDTO> getOutputByMaterialGroup(LocalDate date) {
        String key = "production:outputByMaterialGroup:" + date.format(DF);
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA, key, k -> productionRepository.getOutputByMaterialGroup(date.format(DF)).stream()
                .limit(DEFAULT_TOP_LIMIT)
                .map(this::toOutputItem)
                .toList());
    }

    @AutoWarmUp(order = 120)
    @Override
    public List<ProductionRankItemDTO> getRawConsumptionByMaterial(LocalDate date) {
        String key = "production:rawConsumptionByMaterial:" + date.format(DF);
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA, key, k -> productionRepository.getRawConsumptionByMaterial(date.format(DF)).stream()
                .limit(DEFAULT_TOP_LIMIT)
                .map(this::toRawConsumptionItem)
                .toList());
    }

    @AutoWarmUp(order = 130)
    @Override
    public List<ProductionRankItemDTO> getProductInventory(LocalDate date) {
        String key = "production:productInventory:" + date.format(DF);
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA, key, k -> productionRepository.getProductInventory().stream()
                .limit(DEFAULT_TOP_LIMIT)
                .map(this::toInventoryItem)
                .toList());
    }

    @AutoWarmUp(order = 140)
    @Override
    public List<ProductionRankItemDTO> getThroughputByFactory(LocalDate date) {
        String key = "production:throughputByFactory:" + date.format(DF);
        return caffeineCacheService.getOrLoadList(CacheType.TODAY_DATA, key, k -> productionRepository.getThroughputByFactory(date.format(DF)).stream()
                .map(this::toThroughputItem)
                .toList());
    }

    @Override
    public ProductionDetailPageDTO<ProductionOutputDetailDTO> getOutputDetails(
            LocalDate date, String company, String materialGroup, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionOutputDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countOutputDetails(targetDate, company, materialGroup, keyword));
        result.setList(productionRepository.getOutputDetails(targetDate, company, materialGroup, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toOutputDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionRawConsumptionDetailDTO> getRawConsumptionDetails(
            LocalDate date, String factory, String material, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionRawConsumptionDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countRawConsumptionDetails(targetDate, factory, material, keyword));
        result.setList(productionRepository.getRawConsumptionDetails(targetDate, factory, material, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toRawConsumptionDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionTransportDetailDTO> getTransportDetails(
            LocalDate date, String category, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionTransportDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countTransportDetails(targetDate, category, keyword));
        result.setList(productionRepository.getTransportDetails(targetDate, category, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toTransportDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionEnergyDetailDTO> getEnergyDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionEnergyDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countEnergyDetails(targetDate, keyword));
        result.setList(productionRepository.getEnergyDetails(targetDate, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toEnergyDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionStartupShutdownDetailDTO> getStartupShutdownDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionStartupShutdownDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countStartupShutdownDetails(targetDate, keyword));
        result.setList(productionRepository.getStartupShutdownDetails(targetDate, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toStartupShutdownDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionHighRiskWorkDetailDTO> getHighRiskWorkDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionHighRiskWorkDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countHighRiskWorkDetails(targetDate, keyword));
        result.setList(productionRepository.getHighRiskWorkDetails(targetDate, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toHighRiskWorkDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionToxicGasDetailDTO> getToxicGasDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionToxicGasDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countToxicGasDetails(targetDate, keyword));
        result.setList(productionRepository.getToxicGasDetails(targetDate, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toToxicGasDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionWasteDetailDTO> getWasteDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionWasteDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countWasteDetails(targetDate, keyword));
        result.setList(productionRepository.getWasteDetails(targetDate, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toWasteDetail)
                .toList());
        return result;
    }

    @Override
    public ProductionDetailPageDTO<ProductionWaterGasDetailDTO> getWaterGasDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize) {
        int safePage = safePage(page);
        int safePageSize = safePageSize(pageSize);
        int startRow = (safePage - 1) * safePageSize;
        String targetDate = date.format(DF);

        ProductionDetailPageDTO<ProductionWaterGasDetailDTO> result = new ProductionDetailPageDTO<>();
        result.setPage(safePage);
        result.setPageSize(safePageSize);
        result.setTotal(productionRepository.countWaterGasDetails(targetDate, keyword));
        result.setList(productionRepository.getWaterGasDetails(targetDate, keyword, startRow, startRow + safePageSize)
                .stream()
                .map(this::toWaterGasDetail)
                .toList());
        return result;
    }

    private ProductionRankItemDTO toOutputItem(Map<String, Object> row) {
        ProductionRankItemDTO item = new ProductionRankItemDTO();
        item.setCode(stringValue(row, "CODE"));
        item.setName(stringValue(row, "NAME"));
        item.setValue(decimal(row, "TOTAL_VALUE"));
        item.setFactoryCount(decimal(row, "FACTORY_COUNT").intValue());
        item.setUnit("吨");
        return item;
    }

    private ProductionRankItemDTO toRawConsumptionItem(Map<String, Object> row) {
        ProductionRankItemDTO item = new ProductionRankItemDTO();
        item.setName(stringValue(row, "NAME"));
        item.setUnit(stringValue(row, "UNIT"));
        item.setValue(decimal(row, "TOTAL_VALUE"));
        item.setFactoryCount(decimal(row, "FACTORY_COUNT").intValue());
        return item;
    }

    private ProductionRankItemDTO toInventoryItem(Map<String, Object> row) {
        ProductionRankItemDTO item = new ProductionRankItemDTO();
        item.setFactory(stringValue(row, "FACTORY"));
        item.setName(stringValue(row, "NAME"));
        item.setValue(decimal(row, "TOTAL_VALUE"));
        item.setUnit("吨");
        item.setUpdateTime(stringValue(row, "UPDATE_TIME"));
        return item;
    }

    private ProductionRankItemDTO toThroughputItem(Map<String, Object> row) {
        ProductionRankItemDTO item = new ProductionRankItemDTO();
        item.setFactory(stringValue(row, "FACTORY"));
        item.setCompany(stringValue(row, "COMPANY"));
        item.setCategory(stringValue(row, "CATEGORY"));
        item.setInbound(decimal(row, "INBOUND"));
        item.setOutbound(decimal(row, "OUTBOUND"));
        item.setVehicleCount(decimal(row, "VEHICLE_COUNT"));
        item.setUnit("吨");
        return item;
    }

    private ProductionOutputDetailDTO toOutputDetail(Map<String, Object> row) {
        ProductionOutputDetailDTO item = new ProductionOutputDetailDTO();
        item.setCompany(stringValue(row, "COMPANY"));
        item.setMaterialCode(stringValue(row, "MATERIAL_CODE"));
        item.setMaterialName(stringValue(row, "MATERIAL_NAME"));
        item.setMaterialGroup(stringValue(row, "MATERIAL_GROUP"));
        item.setMaterialGroupName(stringValue(row, "MATERIAL_GROUP_NAME"));
        item.setMonth(stringValue(row, "MONTH_VALUE"));
        item.setPostingDate(stringValue(row, "POSTING_DATE"));
        item.setWorkCenter(stringValue(row, "WORK_CENTER"));
        item.setOutput(decimal(row, "OUTPUT_VALUE"));
        return item;
    }

    private ProductionRawConsumptionDetailDTO toRawConsumptionDetail(Map<String, Object> row) {
        ProductionRawConsumptionDetailDTO item = new ProductionRawConsumptionDetailDTO();
        item.setInputDate(stringValue(row, "INPUT_DATE"));
        item.setPostingDate(stringValue(row, "POSTING_DATE"));
        item.setFactory(stringValue(row, "FACTORY"));
        item.setFactoryName(stringValue(row, "FACTORY_NAME"));
        item.setMaterialCode(stringValue(row, "MATERIAL_CODE"));
        item.setMaterialName(stringValue(row, "MATERIAL_NAME"));
        item.setMaterialGroup(stringValue(row, "MATERIAL_GROUP"));
        item.setMaterialGroupName(stringValue(row, "MATERIAL_GROUP_NAME"));
        item.setStorageLocation(stringValue(row, "STORAGE_LOCATION"));
        item.setUnit(stringValue(row, "UNIT_VALUE"));
        item.setWorkCenter(stringValue(row, "WORK_CENTER"));
        item.setWorkCenterName(stringValue(row, "WORK_CENTER_NAME"));
        item.setOrderNo(stringValue(row, "ORDER_NO"));
        item.setBatchNo(stringValue(row, "BATCH_NO"));
        item.setQuantity(decimal(row, "QUANTITY"));
        item.setProductionPlan(stringValue(row, "PRODUCTION_PLAN"));
        item.setMaterialDocument(stringValue(row, "MATERIAL_DOCUMENT"));
        item.setMovementType(stringValue(row, "MOVEMENT_TYPE"));
        item.setMovementTypeName(stringValue(row, "MOVEMENT_TYPE_NAME"));
        item.setMonth(stringValue(row, "MONTH_VALUE"));
        return item;
    }

    private ProductionTransportDetailDTO toTransportDetail(Map<String, Object> row) {
        ProductionTransportDetailDTO item = new ProductionTransportDetailDTO();
        item.setOrderNo(stringValue(row, "ORDER_NO"));
        item.setBusinessNo(stringValue(row, "BUSINESS_NO"));
        item.setProcessType(stringValue(row, "PROCESS_TYPE"));
        item.setPlateNo(stringValue(row, "PLATE_NO"));
        item.setDriver(stringValue(row, "DRIVER"));
        item.setDriverPhone(stringValue(row, "DRIVER_PHONE"));
        item.setGrossWeight(decimal(row, "GROSS_WEIGHT"));
        item.setTareWeight(decimal(row, "TARE_WEIGHT"));
        item.setNetWeight(decimal(row, "NET_WEIGHT"));
        item.setMaterialName(stringValue(row, "MATERIAL_NAME"));
        item.setWeighingDate(stringValue(row, "WEIGHING_DATE"));
        item.setCustomerName(stringValue(row, "CUSTOMER_NAME"));
        item.setCarrier(stringValue(row, "CARRIER"));
        item.setCompanyName(stringValue(row, "COMPANY_NAME"));
        item.setCategory(stringValue(row, "CATEGORY"));
        return item;
    }

    private ProductionEnergyDetailDTO toEnergyDetail(Map<String, Object> row) {
        ProductionEnergyDetailDTO item = new ProductionEnergyDetailDTO();
        item.setInputDate(stringValue(row, "INPUT_DATE"));
        item.setPostingDate(stringValue(row, "POSTING_DATE"));
        item.setOrderNo(stringValue(row, "ORDER_NO"));
        item.setFactory(stringValue(row, "FACTORY"));
        item.setWater(decimal(row, "WATER"));
        item.setWaterUnit(stringValue(row, "WATER_UNIT"));
        item.setElectricity(decimal(row, "ELECTRICITY"));
        item.setElectricityUnit(stringValue(row, "ELECTRICITY_UNIT"));
        item.setRefrigeration(decimal(row, "REFRIGERATION"));
        item.setRefrigerationUnit(stringValue(row, "REFRIGERATION_UNIT"));
        item.setSteam(decimal(row, "STEAM"));
        item.setSteamUnit(stringValue(row, "STEAM_UNIT"));
        item.setNaturalGas(decimal(row, "NATURAL_GAS"));
        item.setNaturalGasUnit(stringValue(row, "NATURAL_GAS_UNIT"));
        item.setHydrogen(decimal(row, "HYDROGEN"));
        item.setHydrogenUnit(stringValue(row, "HYDROGEN_UNIT"));
        item.setPureWater(decimal(row, "PURE_WATER"));
        item.setPureWaterUnit(stringValue(row, "PURE_WATER_UNIT"));
        return item;
    }

    private ProductionStartupShutdownDetailDTO toStartupShutdownDetail(Map<String, Object> row) {
        ProductionStartupShutdownDetailDTO item = new ProductionStartupShutdownDetailDTO();
        item.setSourceRn(stringValue(row, "SOURCE_RN"));
        item.setCompany(stringValue(row, "COMPANY"));
        item.setDevice(stringValue(row, "DEVICE"));
        item.setTime(stringValue(row, "TIME_VALUE"));
        item.setDev(stringValue(row, "DEV_VALUE"));
        item.setStandard(stringValue(row, "STANDARD_VALUE"));
        item.setSort(stringValue(row, "SORT_VALUE"));
        item.setValue(stringValue(row, "VALUE_TEXT"));
        return item;
    }

    private ProductionHighRiskWorkDetailDTO toHighRiskWorkDetail(Map<String, Object> row) {
        ProductionHighRiskWorkDetailDTO item = new ProductionHighRiskWorkDetailDTO();
        item.setWorkDate(stringValue(row, "WORK_DATE"));
        item.setCompanyName(stringValue(row, "COMPANY_NAME"));
        item.setWorkCount(decimal(row, "WORK_COUNT"));
        return item;
    }

    private ProductionToxicGasDetailDTO toToxicGasDetail(Map<String, Object> row) {
        ProductionToxicGasDetailDTO item = new ProductionToxicGasDetailDTO();
        item.setAlarmDate(stringValue(row, "ALARM_DATE"));
        item.setCompany(stringValue(row, "COMPANY"));
        item.setAlarmCount(decimal(row, "ALARM_COUNT"));
        return item;
    }

    private ProductionWasteDetailDTO toWasteDetail(Map<String, Object> row) {
        ProductionWasteDetailDTO item = new ProductionWasteDetailDTO();
        item.setPostingDate(stringValue(row, "POSTING_DATE"));
        item.setCompanyCode(stringValue(row, "COMPANY_CODE"));
        item.setCompanyName(stringValue(row, "COMPANY_NAME"));
        item.setCompany(stringValue(row, "COMPANY"));
        item.setWasteCode(stringValue(row, "WASTE_CODE"));
        item.setWasteName(stringValue(row, "WASTE_NAME"));
        item.setOutput(decimal(row, "OUTPUT_VALUE"));
        return item;
    }

    private ProductionWaterGasDetailDTO toWaterGasDetail(Map<String, Object> row) {
        ProductionWaterGasDetailDTO item = new ProductionWaterGasDetailDTO();
        item.setPointCode(stringValue(row, "POINT_CODE"));
        item.setPointName(stringValue(row, "POINT_NAME"));
        item.setSubName(stringValue(row, "SUB_NAME"));
        item.setItemDesc(stringValue(row, "ITEM_DESC"));
        item.setGenerateTime(stringValue(row, "GENERATE_TIME_VALUE"));
        item.setGroupTime(stringValue(row, "GROUP_TIME"));
        return item;
    }

    private int safePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }

    private ProductionMetricDTO metric(String label, BigDecimal value, String unit, String description) {
        return new ProductionMetricDTO(label, value, unit, description);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.multiply(BigDecimal.valueOf(100)).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(Map<String, Object> row, String key) {
        if (row == null) {
            return BigDecimal.ZERO;
        }
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toLowerCase());
        }
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String stringValue(Map<String, Object> row, String key) {
        if (row == null) {
            return "";
        }
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toLowerCase());
        }
        return Objects.toString(value, "");
    }
}
