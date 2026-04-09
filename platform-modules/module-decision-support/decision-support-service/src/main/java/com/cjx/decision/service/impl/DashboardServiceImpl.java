package com.cjx.decision.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.cjx.decision.dto.dashboard.*;
import com.cjx.decision.dto.salesdetail.*;
import com.cjx.decision.entity.frorcl.CustomerTransactionDTO;
import com.cjx.decision.entity.frorcl.RawPriceDeviation;
import com.cjx.decision.entity.frorcl.SalesSummary;
import com.cjx.decision.service.DashboardService;
import com.cjx.decision.service.SalesService;
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
 * @author cuijixu
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final SalesService salesService;


    /**
     * 查询核心指标 (查询快)
     */
    @Override
    public DashboardMetricsDTO getMetrics(LocalDate date) {
        SalesSummary salesSummary = salesService.findSummaryByDate(date);//昨日
        SalesSummary todaySum = salesService.findSummaryToToday(date); //累计
        SalesSummary countBudget = salesService.findCountBudget(date); //销量预算
        SalesSummary amountBudget = salesService.findAmountBudget(date); //销售额预算
        SalesSummary collectionMonth = salesService.findCollection(date);//本月回款
        DashboardMetricsDTO dto = new DashboardMetricsDTO();
        RawSalesMetric volume = new RawSalesMetric();
        volume.setMetricName("总销量");
        volume.setDisplayValue(salesSummary.getTotalSales()+" 吨");
        volume.setBudgetRate(todaySum.getTotalSales()/countBudget.getTotalCountBudget());//完成率
        volume.setGapValue(todaySum.getTotalSales());//差额
        volume.setMonthGoal(countBudget.getTotalCountBudget());//本月目标。月预算
        volume.setType("volume");
        dto.setSalesVolume(volume);

        RawSalesMetric amount = new RawSalesMetric();
        amount.setMetricName("总销售额");
        amount.setDisplayValue(salesSummary.getTotalAmount()+" 万元");
        amount.setBudgetRate(todaySum.getTotalAmount()/amountBudget.getTotalAmountBudget());
        amount.setGapValue(todaySum.getTotalAmount());
        amount.setMonthGoal(amountBudget.getTotalAmountBudget());
        amount.setType("amount");
        dto.setSalesAmount(amount);
        RawCollection collection = new RawCollection();
        if(Objects.nonNull(collectionMonth)){
            collection.setCollectionAmount(collectionMonth.getCollection()+" 万元");
            collection.setCollectionRate(collectionMonth.getCollection()/todaySum.getTotalAmount());
            collection.setGapValue(todaySum.getTotalAmount()-collectionMonth.getCollection());
            collection.setMonthGoal(todaySum.getTotalAmount());
        }
        dto.setCollection(collection);
        return dto;
    }

    @Override
    public DashboardOrdersDTO getOrders(LocalDate targetDate) {
        DashboardOrdersDTO dto = new DashboardOrdersDTO();

        SalesSummary monthOrder = salesService.findMonthOrder(targetDate);
        RawOrder monthOrders = new RawOrder();
        monthOrders.setOrderTitle("本月未关订单数");
        monthOrders.setOrderCount(monthOrder.getOpenOrder()+"");
        monthOrders.setOrderRate(monthOrder.getOpenOrder().divide(monthOrder.getTotalOrder(), 2, RoundingMode.HALF_UP));
        monthOrders.setBarColor("#f59e0b");
        dto.setMonthOrders(monthOrders);

        SalesSummary yearOrder = salesService.findYearOrder(targetDate);
        RawOrder yearOrders = new RawOrder();
        yearOrders.setOrderTitle("本年未关订单数");
        yearOrders.setOrderCount(yearOrder.getOpenOrder()+"");
        yearOrders.setOrderRate(yearOrder.getOpenOrder().divide(yearOrder.getTotalOrder(), 2, RoundingMode.HALF_UP));
        yearOrders.setBarColor("#f59e0b");
        dto.setYearOrders(yearOrders);

        return dto;
    }

    @Override
    public List<RawPriceDeviation> getPriceDeviations(LocalDate date) {
        return salesService.findPriceDiff(date);
    }

    @Override
    public List<CustomerTransactionDTO> findCustomerTransaction(String region, String code) {
        String yesterday = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return salesService.findCustomerTransaction(region,code,yesterday);
    }

    @Override
    /**
     * 获取销售明细看板数据 (提供给前端直接使用)
     */
    public List<CompanyMetricDTO> getCompanyList(String type, LocalDate date) {
        return salesService.findSaleDetails(type,date);
    }

    @Override
    public List<CompanyDetailDTO> getCompanyDetail(String companyName, String type, LocalDate date, String target) {
        if ("绿冷".equals(companyName)){
            companyName = "3001";
        }else if ("有机硅".equals(companyName)){
            companyName = "1400";
        }else if ("氟硅".equals(companyName)){
            companyName = "1301";
        }else if ("高分子".equals(companyName)){
            companyName = "1201";
        }
        List<SalesSummary> dailySalesSummaries = salesService.findSummaryByCompany(date,companyName);
        List<SalesSummary> productSummaries = salesService.findDetailsByCompany(date,companyName);
        List<BigDecimal> dailySales = new ArrayList<>();
        List<ProductMetricDTO> products = new ArrayList<>();
        if ("volume".equals(type)){
            dailySalesSummaries.forEach(salesSummary -> {
                dailySales.add(BigDecimal.valueOf(salesSummary.getTotalSales()));
            });
            productSummaries.forEach(product -> {
                ProductMetricDTO productMetricDTO = new ProductMetricDTO();
                productMetricDTO.setProductName(product.getProductName());
                productMetricDTO.setProductCode(product.getProductCode());
                productMetricDTO.setValue(BigDecimal.valueOf(product.getTotalSales()));
                productMetricDTO.setPercentage(product.getSalesRatio());
                productMetricDTO.setRegion(product.getRegion());
                products.add(productMetricDTO);
            });
        }else {
            dailySalesSummaries.forEach(salesSummary -> {
                dailySales.add(BigDecimal.valueOf(salesSummary.getTotalAmount()));
            });
            productSummaries.forEach(product -> {
                ProductMetricDTO productMetricDTO = new ProductMetricDTO();
                productMetricDTO.setProductName(product.getProductName());
                productMetricDTO.setProductCode(product.getProductCode());
                productMetricDTO.setValue(BigDecimal.valueOf(product.getTotalAmount()));
                productMetricDTO.setPercentage(product.getAmountRatio());
                productMetricDTO.setRegion(product.getRegion());
                products.add(productMetricDTO);
            });
        }
        List<CompanyDetailDTO> result = new ArrayList<>();
        CompanyDetailDTO companyDetailDTO = new CompanyDetailDTO();
        companyDetailDTO.setProducts(products);
        companyDetailDTO.setDailySales(dailySales);
        result.add(companyDetailDTO);
        return result;
    }

    @Override
    public List<SalesTrendProductDTO> getSalesTrends(String date) {
        List<SalesTrendProductDTO> resultList = new ArrayList<>();
        LocalDate endLocalDate = LocalDate.now().minusDays(1);
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        getMonthTrends(resultList, endLocalDate, endDate);
        return resultList;
    }

    @Override
    public List<SalesTrendPointDTO> getSalesTrendsList(String productCode,String region,String date) {
        List<SalesTrendPointDTO> resultList = new ArrayList<>();
        LocalDate endLocalDate = LocalDate.now().minusDays(1);
        String endDate = endLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        getYearTrends(resultList, endLocalDate, endDate,productCode,region);
        return resultList;
    }

    @Override
    public ProductDeepDetail getProductDeepDetail(String companyName, String productCode, String type, LocalDate date) {
        ProductDeepDetail result = new ProductDeepDetail();
        List<ProductDeepTrend> deepTrends = new ArrayList<>();
        ProductDeepKPI kpi = new ProductDeepKPI();

        List<ProductDeepCustomer> customerList = new ArrayList<>();
        List<SalesSummary> customers = salesService.getProductCustomer(companyName,productCode,date);
        for (int i = 0; i < 10; i++) {
            SalesSummary salesSummary = customers.get(i);
            ProductDeepCustomer customer = new ProductDeepCustomer();
            customer.setName(salesSummary.getCustomer());
            customer.setVolume(BigDecimal.valueOf(salesSummary.getTotalSales()));
            customerList.add(customer);
        }
        if ("month".equals(type)){
            List<SalesSummary> monthList = salesService.getProductDeepMonth(companyName,productCode,date);
            deepTrends = buildTrendData(kpi,monthList);
        }else {
            List<SalesSummary> yearList = salesService.getProductDeepYear(companyName,productCode,date);
            deepTrends = buildTrendData(kpi,yearList);
        }
        result.setTrend(deepTrends);
        result.setKpi(kpi);
        result.setTopCustomers(customerList);
        return result;
    }

    @Override
    public List<CompanyMetricDTO> getCollectionCompanies(LocalDate date) {
//value 当月回款，target 计划回款。companyName 公司名
        return null;
    }


    private List<ProductDeepTrend> buildTrendData(ProductDeepKPI kpi,List<SalesSummary> list){
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
                BigDecimal recordVolume = record.getTotalSales() != null ? BigDecimal.valueOf(record.getTotalSales()) : BigDecimal.ZERO;
                BigDecimal recordAmount = record.getTotalAmount() != null ? BigDecimal.valueOf(record.getTotalAmount()) : BigDecimal.ZERO;
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
        kpi.setProfitEst("123");
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

                double currentSalesDouble = (po != null && po.getTotalSales() != null) ? po.getTotalSales() : 0.0;
                BigDecimal currentVolume = BigDecimal.valueOf(currentSalesDouble);
                BigDecimal currentPrice = (po != null && po.getPrice() != null) ? po.getPrice() : BigDecimal.ZERO;

                volumeArray[i] = currentSalesDouble;
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

    private void getYearTrends(List<SalesTrendPointDTO> resultList, LocalDate endLocalDate, String endDate,String productCode,String region){
        List<SalesSummary> trendsAll = salesService.findTrendsYear(endLocalDate,productCode,region);
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
                double volume = summary.getTotalSales() != null ? summary.getTotalSales() : 0.0;
                BigDecimal price = summary.getPrice() != null ? summary.getPrice() : BigDecimal.ZERO;

                resultList.add(new SalesTrendPointDTO(monthStr, BigDecimal.valueOf(volume), price));
            } else {
                resultList.add(new SalesTrendPointDTO(monthStr, BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }
    }
}
