package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.enums.RegionCode;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.repository.frorcl.PriceAnalysisRepository;
import com.cjx.decision.service.PriceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 价格分析服务实现
 * 负责查询价格偏差和客户交易数据
 *
 * @author system
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PriceAnalysisServiceImpl implements PriceAnalysisService {

    private final PriceAnalysisRepository priceAnalysisRepository;
    private final CaffeineCacheService caffeineCacheService;

    @Override
    public List<RawPriceDeviation> getPriceDeviations(LocalDate date) {
        String startDate = date.minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = date.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "priceDiff:" + endDate;
        
        return caffeineCacheService.getOrLoadList(
            CacheType.TODAY_DATA, key,
            k -> priceAnalysisRepository.findPriceDiff(startDate, endDate)
        );
    }

    @Override
    public List<CustomerTransactionProjection> getCustomerTransactions(String region, String code) {
        String regionCode = RegionCode.getCodeByName(region);
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "customerTransaction:" + yesterday + ":" + code;
        return priceAnalysisRepository.findCustomerTransaction(regionCode, code, yesterday);
    }
}
