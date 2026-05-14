package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.annotation.AutoWarmUp;
import com.cjx.decision.dto.production.ProductionDetailPageDTO;
import com.cjx.decision.dto.production.ProductionMetricDTO;
import com.cjx.decision.dto.production.ProductionOverviewDTO;
import com.cjx.decision.dto.production.ProductionOutputDetailDTO;
import com.cjx.decision.dto.production.ProductionRankItemDTO;
import com.cjx.decision.dto.production.ProductionRawConsumptionDetailDTO;
import com.cjx.decision.dto.production.ProductionThroughputDTO;
import com.cjx.decision.dto.production.ProductionTransportDetailDTO;
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

    private int safePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
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
