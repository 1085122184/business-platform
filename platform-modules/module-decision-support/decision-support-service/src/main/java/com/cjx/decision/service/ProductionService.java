package com.cjx.decision.service;

import com.cjx.decision.dto.production.ProductionOverviewDTO;
import com.cjx.decision.dto.production.ProductionDetailPageDTO;
import com.cjx.decision.dto.production.ProductionEnergyDetailDTO;
import com.cjx.decision.dto.production.ProductionHighRiskWorkDetailDTO;
import com.cjx.decision.dto.production.ProductionOutputDetailDTO;
import com.cjx.decision.dto.production.ProductionRankItemDTO;
import com.cjx.decision.dto.production.ProductionRawConsumptionDetailDTO;
import com.cjx.decision.dto.production.ProductionStartupShutdownDetailDTO;
import com.cjx.decision.dto.production.ProductionToxicGasDetailDTO;
import com.cjx.decision.dto.production.ProductionTransportDetailDTO;
import com.cjx.decision.dto.production.ProductionWasteDetailDTO;
import com.cjx.decision.dto.production.ProductionWaterGasDetailDTO;

import java.time.LocalDate;
import java.util.List;

public interface ProductionService {
    ProductionOverviewDTO getOverview(LocalDate date);

    List<ProductionRankItemDTO> getOutputByMaterialGroup(LocalDate date);

    List<ProductionRankItemDTO> getRawConsumptionByMaterial(LocalDate date);

    List<ProductionRankItemDTO> getProductInventory(LocalDate date);

    List<ProductionRankItemDTO> getThroughputByFactory(LocalDate date);

    ProductionDetailPageDTO<ProductionOutputDetailDTO> getOutputDetails(
            LocalDate date, String company, String materialGroup, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionRawConsumptionDetailDTO> getRawConsumptionDetails(
            LocalDate date, String factory, String material, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionTransportDetailDTO> getTransportDetails(
            LocalDate date, String category, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionEnergyDetailDTO> getEnergyDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionStartupShutdownDetailDTO> getStartupShutdownDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionHighRiskWorkDetailDTO> getHighRiskWorkDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionToxicGasDetailDTO> getToxicGasDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionWasteDetailDTO> getWasteDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize);

    ProductionDetailPageDTO<ProductionWaterGasDetailDTO> getWaterGasDetails(
            LocalDate date, String keyword, Integer page, Integer pageSize);
}
