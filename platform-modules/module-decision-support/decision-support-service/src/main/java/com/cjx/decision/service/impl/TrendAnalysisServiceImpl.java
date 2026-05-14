package com.cjx.decision.service.impl;

import com.cjx.common.core.enums.CacheType;
import com.cjx.common.core.utils.CaffeineCacheService;
import com.cjx.decision.annotation.AutoWarmUp;
import com.cjx.decision.constant.CompanyCodeConstant;
import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.dashboard.SalesTrendProductDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepCustomer;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.dto.salesdetail.ProductDeepKPI;
import com.cjx.decision.dto.salesdetail.ProductDeepTrend;
import com.cjx.decision.enums.AnalysisType;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.repository.frorcl.TrendAnalysisRepository;
import com.cjx.decision.service.TrendAnalysisService;
import com.cjx.decision.utils.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 趋势分析服务实现
 * 负责查询销售趋势和产品深度数据
 *
 * @author system
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class TrendAnalysisServiceImpl implements TrendAnalysisService {

    private final TrendAnalysisRepository trendAnalysisRepository;
    private final CaffeineCacheService caffeineCacheService;

    @AutoWarmUp(order = 50)
    @Override
    public List<SalesTrendProductDTO> getMonthlyTrends(LocalDate date) {
        List<SalesTrendProductDTO> resultList = new ArrayList<>();
        LocalDate endLocalDate = date.minusDays(1);
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        buildMonthTrends(resultList, endLocalDate, endDate);
        return resultList;
    }

    @Override
    public List<SalesTrendPointDTO> getYearlyTrends(String productCode, String region, LocalDate date) {
        List<SalesTrendPointDTO> resultList = new ArrayList<>();
        LocalDate endLocalDate = date.minusDays(1);
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        buildYearTrends(resultList, endLocalDate, endDate, productCode, region);
        return resultList;
    }

    @Override
    public ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date) {
        AnalysisType analysisType = AnalysisType.fromCode(type);
        String key = "productDeep:" + companyName + ":" + productCode + ":" + type + ":" + date;
        return caffeineCacheService.getOrLoad(
            CacheType.TODAY_DATA, key,
            k -> buildProductDeepDetail(companyName, productCode, analysisType, date),
            ProductDeepDetail.class
        );
    }

    private ProductDeepDetail buildProductDeepDetail(String companyName, String productCode, AnalysisType type, LocalDate date) {
        ProductDeepDetail result = new ProductDeepDetail();
        List<ProductDeepTrend> deepTrends;
        ProductDeepKPI kpi = new ProductDeepKPI();
        companyName = CompanyCodeConstant.COMPANY_CODE_MAP.getOrDefault(companyName, companyName);
        List<ProductDeepCustomer> customerList = new ArrayList<>();
        List<SalesSummary> customers = new ArrayList<>();
        if (type == AnalysisType.MONTH) {
            customers = trendAnalysisRepository.getProductCustomerMonth(companyName, productCode, date);
            List<SalesSummary> monthList = trendAnalysisRepository.getProductDeepMonth(companyName, productCode, date);
            deepTrends = buildTrendData(kpi, monthList);
        } else {
            customers = trendAnalysisRepository.getProductCustomer(companyName, productCode, date);
            List<SalesSummary> yearList = trendAnalysisRepository.getProductDeepYear(companyName, productCode, date);
            deepTrends = buildTrendData(kpi, yearList);
        }

        int limit = Math.min(20, customers.size());
        for (int i = 0; i < limit; i++) {
            SalesSummary salesSummary = customers.get(i);
            ProductDeepCustomer customer = new ProductDeepCustomer();
            customer.setName(salesSummary.getCustomer());
            customer.setVolume(salesSummary.getTotalSales());
            customer.setAmount(salesSummary.getTotalAmount());
            customerList.add(customer);
        }
        result.setTrend(deepTrends);
        result.setKpi(kpi);
        result.setTopCustomers(customerList);
        return result;
    }

    private List<ProductDeepTrend> buildTrendData(ProductDeepKPI kpi, List<SalesSummary> list) {
        BigDecimal allVolume = BigDecimal.ZERO;
        BigDecimal allDomesticVolume = BigDecimal.ZERO;
        BigDecimal allIntVolume = BigDecimal.ZERO;
        BigDecimal allAmount = BigDecimal.ZERO;
        BigDecimal allDomesticAmount = BigDecimal.ZERO;
        BigDecimal allIntAmount = BigDecimal.ZERO;
        Map<String, List<SalesSummary>> groupedByDate = list.stream()
                .collect(Collectors.groupingBy(SalesSummary::getLatestDate));
        List<ProductDeepTrend> resultList = new ArrayList<>();
        for (Map.Entry<String, List<SalesSummary>> entry : groupedByDate.entrySet()) {
            String fullDate = entry.getKey(); // 例如 "2026-03-01"
            List<SalesSummary> dailyRecords = entry.getValue();

            // 截取 MM-DD 给前端显示（如果前端需要完整的就不用截取）
            String displayDate = fullDate.length() >= 10 ? fullDate.substring(5, 10) : fullDate;

            BigDecimal domesticVolume = BigDecimal.ZERO;
            BigDecimal intlVolume = BigDecimal.ZERO;
            BigDecimal domesticAmount = BigDecimal.ZERO;
            BigDecimal intlAmount = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            // 汇总当天的所有记录
            for (SalesSummary record : dailyRecords) {
                boolean isDomestic = "10".equals(record.getRegion());
                BigDecimal recordVolume = record.getTotalSales() != null ? record.getTotalSales() : BigDecimal.ZERO;
                BigDecimal recordAmount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                if (isDomestic) {
                    domesticVolume = domesticVolume.add(recordVolume);
                    domesticAmount = domesticAmount.add(recordAmount);
                    allDomesticVolume = allDomesticVolume.add(recordVolume);
                    allDomesticAmount = allDomesticAmount.add(recordAmount);
                } else {
                    intlVolume = intlVolume.add(recordVolume);
                    intlAmount = intlAmount.add(recordAmount);
                    allIntVolume = allIntVolume.add(recordVolume);
                    allIntAmount = allIntAmount.add(recordAmount);
                }

                // 无论国内国外，当天的销售额都累加
                totalAmount = totalAmount.add(recordAmount);
                allVolume = allVolume.add(recordVolume);
                allAmount = allAmount.add(recordAmount);
            }

            // 3. 构建前端需要的对象
            ProductDeepTrend trendObj = new ProductDeepTrend();
            trendObj.setDate(displayDate);
            trendObj.setDomesticVolume(domesticVolume);
            trendObj.setIntlVolume(intlVolume);
            trendObj.setDomesticAmount(domesticAmount);
            trendObj.setIntlAmount(intlAmount);
            trendObj.setAmount(totalAmount);
            resultList.add(trendObj);
        }
        kpi.setAvgPrice(
                allVolume.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : allAmount.divide(allVolume, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(10000))
        );
        kpi.setTotalVolume(allVolume);
        kpi.setTotalAmount(allAmount);
        kpi.setDomesticVolume(allDomesticVolume);
        kpi.setIntlVolume(allIntVolume);
        kpi.setDomesticAmount(allDomesticAmount);
        kpi.setIntlAmount(allIntAmount);
        kpi.setDomesticAvgPrice(
                allDomesticVolume.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : allDomesticAmount.divide(allDomesticVolume, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(10000))
        );
        kpi.setIntlAvgPrice(
                allIntVolume.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : allIntAmount.divide(allIntVolume, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(10000))
        );
        // TODO: 待实现利润估算逻辑
        kpi.setProfitEst(null);
        // 4. 按日期升序排序，保证 ECharts 折线图不会乱跑
        resultList.sort(Comparator.comparing(ProductDeepTrend::getDate));
        return resultList;
    }

    private void buildMonthTrends(List<SalesTrendProductDTO> resultList, LocalDate endLocalDate, String endDate) {
        String startDate = endLocalDate.minusDays(29).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<SalesSummary> trendsAll = caffeineCacheService.getOrLoadList(
            CacheType.TODAY_DATA, "trendsAll:" + endDate,
            k -> trendAnalysisRepository.findTrendsAll(startDate, endDate)
        );
        Map<String, List<SalesSummary>> groupedData = trendsAll.stream()
                .collect(Collectors.groupingBy(po -> po.getRegion() + "_" + po.getProductCode()));

        for (Map.Entry<String, List<SalesSummary>> entry : groupedData.entrySet()) {
            List<SalesSummary> historyList = entry.getValue();
            SalesSummary baseInfo = historyList.get(0);

            Map<String, SalesSummary> dateDataMap = historyList.stream()
                    .collect(Collectors.toMap(SalesSummary::getLatestDate, po -> po, (v1, v2) -> v1));

            int days = 30;
            double[] volumeArray = new double[days];
            double[] priceArray = new double[days];
            List<SalesTrendPointDTO> trendList = new ArrayList<>(days);
            BigDecimal yesterdayVol = BigDecimal.ZERO;
            BigDecimal yesterdayPrice = BigDecimal.ZERO;
            BigDecimal todayVol = BigDecimal.ZERO;
            BigDecimal todayPrice = BigDecimal.ZERO;

            for (int i = 0; i < days; i++) {
                LocalDate currentDate = endLocalDate.minusDays((days - 1) - i);
                String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                SalesSummary po = dateDataMap.get(dateStr);

                BigDecimal currentVolume = (po != null && po.getTotalSales() != null) ? po.getTotalSales() : BigDecimal.ZERO;
                BigDecimal currentPrice = (po != null && po.getPrice() != null) ? po.getPrice() : BigDecimal.ZERO;

                volumeArray[i] = currentVolume.doubleValue();
                priceArray[i] = currentPrice.doubleValue();

                trendList.add(new SalesTrendPointDTO(dateStr, currentVolume, currentPrice));

                if (i == days - 2) {
                    yesterdayVol = currentVolume;
                    yesterdayPrice = currentPrice;
                } else if (i == days - 1) {
                    todayVol = currentVolume;
                    todayPrice = currentPrice;
                }
            }

            double volumeChange = 0.0;
            double priceChange = 0.0;

            if (yesterdayVol.compareTo(BigDecimal.ZERO) != 0) {
                volumeChange = todayVol.subtract(yesterdayVol)
                        .divide(yesterdayVol, 4, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            if (yesterdayPrice.compareTo(BigDecimal.ZERO) != 0) {
                priceChange = todayPrice.subtract(yesterdayPrice)
                        .divide(yesterdayPrice, 4, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            double rawCorrelation = MathUtil.getPearsonCorrelation(volumeArray, priceArray);
            double finalCorrelation = Math.round(rawCorrelation * 100.0) / 100.0;

            SalesTrendProductDTO dto = SalesTrendProductDTO.builder()
                    .productCode(baseInfo.getProductCode())
                    .product(baseInfo.getProductName())
                    .region(baseInfo.getRegion())
                    .latestDate(endDate)
                    .latestVolume(todayVol)
                    .latestPrice(todayPrice)
                    .volumeChange(volumeChange)
                    .priceChange(priceChange)
                    .correlation(finalCorrelation)
                    .trend(trendList)
                    .build();

            resultList.add(dto);
        }
    }

    private void buildYearTrends(List<SalesTrendPointDTO> resultList, LocalDate endLocalDate, String endDate, String productCode, String region) {
        LocalDate firstDayOfYear = LocalDate.of(endLocalDate.getYear(), 1, 1);
        String startDate = firstDayOfYear.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "trendsYear:" + endDate + ":" + productCode + "_" + region;
        
        List<SalesSummary> trendsAll = caffeineCacheService.getOrLoadList(
            CacheType.TODAY_DATA, key,
            k -> trendAnalysisRepository.findTrendsYear(startDate, endDate, productCode, region)
        );
        
        Map<String, SalesSummary> monthDataMap = trendsAll.stream()
                .collect(Collectors.toMap(
                        SalesSummary::getLatestDate,
                        item -> item,
                        (v1, v2) -> v1
                ));
        int currentYear = endLocalDate.getYear();
        for (int month = 1; month <= 12; month++) {
            String monthStr = String.format("%04d-%02d", currentYear, month);

            SalesSummary summary = monthDataMap.get(monthStr);

            if (summary != null) {
                BigDecimal volume = summary.getTotalSales() != null ? summary.getTotalSales() : BigDecimal.ZERO;
                BigDecimal price = summary.getPrice() != null ? summary.getPrice() : BigDecimal.ZERO;

                resultList.add(new SalesTrendPointDTO(monthStr, volume, price));
            } else {
                resultList.add(new SalesTrendPointDTO(monthStr, BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }
    }
}
