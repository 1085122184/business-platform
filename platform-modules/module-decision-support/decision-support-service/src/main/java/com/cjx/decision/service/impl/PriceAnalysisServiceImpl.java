package com.cjx.decision.service.impl;

import com.cjx.decision.enums.RegionCode;
import com.cjx.decision.projection.frorcl.CustomerTransactionProjection;
import com.cjx.decision.projection.frorcl.RawPriceDeviation;
import com.cjx.decision.service.PriceAnalysisService;
import com.cjx.decision.service.SalesService;
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
    
    private final SalesService salesService;

    @Override
    public List<RawPriceDeviation> getPriceDeviations(LocalDate date) {
        return salesService.findPriceDiff(date);
    }

    @Override
    public List<CustomerTransactionProjection> getCustomerTransactions(String region, String code) {
        // 将区域名称转换为代码
        String regionCode = RegionCode.getCodeByName(region);
        String yesterday = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return salesService.findCustomerTransaction(regionCode, code, yesterday);
    }
}
