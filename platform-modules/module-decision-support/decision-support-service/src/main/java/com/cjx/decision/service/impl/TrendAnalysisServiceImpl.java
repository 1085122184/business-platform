package com.cjx.decision.service.impl;

import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import com.cjx.decision.dto.dashboard.SalesTrendProductDTO;
import com.cjx.decision.dto.salesdetail.ProductDeepCustomer;
import com.cjx.decision.dto.salesdetail.ProductDeepDetail;
import com.cjx.decision.dto.salesdetail.ProductDeepKPI;
import com.cjx.decision.dto.salesdetail.ProductDeepTrend;
import com.cjx.decision.enums.AnalysisType;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.service.SalesService;
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
    
    private final SalesService salesService;

    @Override
    public List<SalesTrendProductDTO> getMonthlyTrends(String date) {
        List<SalesTrendProductDTO> resultList = new ArrayList<>();
        LocalDate endLocalDate = LocalDate.now().minusDays(1);
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        getMonthTrends(resultList, endLocalDate, endDate);
        return resultList;
    }

    @Override
    public List<SalesTrendPointDTO> getYearlyTrends(String productCode, String region, String date) {
        List<SalesTrendPointDTO> resultList = new ArrayList<>();
        LocalDate endLocalDate = LocalDate.now().minusDays(1);
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        getYearTrends(resultList, endLocalDate, endDate, productCode, region);
        return resultList;
    }

    @Override
    public ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date) {
        ProductDeepDetail result = new ProductDeepDetail();
        List<ProductDeepTrend> deepTrends = new ArrayList<>();
        ProductDeepKPI kpi = new ProductDeepKPI();

        List<ProductDeepCustomer> customerList = new ArrayList<>();
        List<SalesSummary> customers = salesService.getProductCustomer(companyName, productCode, date);
        int limit = Math.min(10, customers.size());
        for (int i = 0; i < limit; i++) {
            SalesSummary salesSummary = customers.get(i);
            ProductDeepCustomer customer = new ProductDeepCustomer();
            customer.setName(salesSummary.getCustomer());
            customer.setVolume(salesSummary.getTotalSales());
            customerList.add(customer);
        }
        AnalysisType analysisType = AnalysisType.fromCode(type);
        if (analysisType == AnalysisType.MONTH) {
            List<SalesSummary> monthList = salesService.getProductDeepMonth(companyName, productCode, date);
            deepTrends = buildTrendData(kpi, monthList);
        } else {
            List<SalesSummary> yearList = salesService.getProductDeepYear(companyName, productCode, date);
            deepTrends = buildTrendData(kpi, yearList);
        }
        result.setTrend(deepTrends);
        result.setKpi(kpi);
        result.setTopCustomers(customerList);
        return result;
    }

    private List<ProductDeepTrend> buildTrendData(ProductDeepKPI kpi, List<SalesSummary> list) {
        BigDecimal allVolume = BigDecimal.ZERO;
        BigDecimal allAmount = BigDecimal.ZERO;
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
            BigDecimal totalAmount = BigDecimal.ZERO;

            // 汇总当天的所有记录
            for (SalesSummary record : dailyRecords) {
                boolean isDomestic = "10".equals(record.getRegion());
                BigDecimal recordVolume = record.getTotalSales() != null ? record.getTotalSales() : BigDecimal.ZERO;
                BigDecimal recordAmount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                if (isDomestic) {
                    domesticVolume = domesticVolume.add(recordVolume);
                } else {
                    intlVolume = intlVolume.add(recordVolume);
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
            trendObj.setAmount(totalAmount);
            resultList.add(trendObj);
        }
        kpi.setAvgPrice(allAmount.divide(allVolume, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(10000)));
        kpi.setTotalVolume(allVolume);
        // TODO: 待实现利润估算逻辑
        kpi.setProfitEst(null);
        // 4. 按日期升序排序，保证 ECharts 折线图不会乱跑
        resultList.sort(Comparator.comparing(ProductDeepTrend::getDate));
        return resultList;
    }

    private void getMonthTrends(List<SalesTrendProductDTO> resultList, LocalDate endLocalDate, String endDate) {
        List<SalesSummary> trendsAll = salesService.findTrendsAll(endLocalDate);
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

    private void getYearTrends(List<SalesTrendPointDTO> resultList, LocalDate endLocalDate, String endDate, String productCode, String region) {
        List<SalesSummary> trendsAll = salesService.findTrendsYear(endLocalDate, productCode, region);
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
