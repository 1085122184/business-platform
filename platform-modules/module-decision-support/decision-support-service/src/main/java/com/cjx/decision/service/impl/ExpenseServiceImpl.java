package com.cjx.decision.service.impl;

import com.cjx.decision.dto.expense.*;
import com.cjx.decision.repository.frorcl.ExpenseRepository;
import com.cjx.decision.service.ExpenseService;
import com.cjx.decision.utils.MathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MF = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final BigDecimal BILLION_DIVISOR = new BigDecimal("1"); //

    @Override
    public ExpenseOverviewDTO getOverview(LocalDate date) {
        String curr = date.format(MF);
        String last = date.minusMonths(1).format(MF);
        Map<String, Object> raw = expenseRepository.getComparisonData(curr, last);

        ExpenseOverviewDTO dto = new ExpenseOverviewDTO();

        // 分别组装销售、管理、财务费用的结构体
        ExpenseOverviewDTO.MetricDetail sales = buildMetric(raw, "CUR_SALES", "LST_SALES");
        ExpenseOverviewDTO.MetricDetail manage = buildMetric(raw, "CUR_MANAGE", "LST_MANAGE");
        ExpenseOverviewDTO.MetricDetail finance = buildMetric(raw, "CUR_FINANCE", "LST_FINANCE");

        // 计算总额和同比
        double curTotal = getDoubleValue(raw, "CUR_SALES") + getDoubleValue(raw, "CUR_MANAGE") + getDoubleValue(raw, "CUR_FINANCE");
        double lstTotal = getDoubleValue(raw, "LST_SALES") + getDoubleValue(raw, "LST_MANAGE") + getDoubleValue(raw, "LST_FINANCE");

        // 计算各项占比
        sales.setPercent(BigDecimal.valueOf(MathUtil.calculatePercent(getDoubleValue(raw, "CUR_SALES"), curTotal)));
        manage.setPercent(BigDecimal.valueOf(MathUtil.calculatePercent(getDoubleValue(raw, "CUR_MANAGE"), curTotal)));
        finance.setPercent(BigDecimal.valueOf(MathUtil.calculatePercent(getDoubleValue(raw, "CUR_FINANCE"), curTotal)));

        ExpenseOverviewDTO.MetricDetail total = new ExpenseOverviewDTO.MetricDetail();
        total.setAmount(BigDecimal.valueOf(curTotal).divide(BILLION_DIVISOR, 2, RoundingMode.HALF_UP));
        total.setYoyChange(BigDecimal.valueOf(MathUtil.calculateYoy(curTotal, lstTotal)));
        total.setUnit("万");

        // 生成文字描述 (例如：同比上升 ¥1.25亿)
        double diff = (curTotal - lstTotal);
        String desc = diff > 0 ? String.format("同比上升 ¥%.2f万", diff) : String.format("同比下降 ¥%.2f万", Math.abs(diff));
        total.setYoyChangeText(desc);

        dto.setTotalExpense(total);
        dto.setSalesExpense(sales);
        dto.setManagementExpense(manage);
        dto.setFinanceExpense(finance);
        return dto;
    }

    @Override
    public  List<ExpenseStructureDTO> getStructure(LocalDate date) {
        List<ExpenseStructureDTO> result = new ArrayList<>();
        // 复用总览数据来计算占比
        ExpenseOverviewDTO overview = this.getOverview(date);
        ExpenseStructureDTO sales = new ExpenseStructureDTO();
        sales.setName("销售费用");
        sales.setValue(overview.getSalesExpense().getAmount());
        sales.setPercent(overview.getSalesExpense().getPercent());
        result.add(sales);
        ExpenseStructureDTO manage = new ExpenseStructureDTO();
        manage.setName("管理费用");
        manage.setValue(overview.getManagementExpense().getAmount());
        manage.setPercent(overview.getManagementExpense().getPercent());
        result.add(manage);
        ExpenseStructureDTO finance = new ExpenseStructureDTO();
        finance.setName("财务费用");
        finance.setValue(overview.getFinanceExpense().getAmount());
        finance.setPercent(overview.getFinanceExpense().getPercent());
        result.add(finance);
//        result.add(new ExpenseStructureDTO.StructureItem("销售费用", overview.getSalesExpense().getAmount(), overview.getSalesExpense().getPercent()));
//        result.add(new ExpenseStructureDTO.StructureItem("管理费用", overview.getManagementExpense().getAmount(), overview.getManagementExpense().getPercent()));
//        result.add(new ExpenseStructureDTO.StructureItem("财务费用", overview.getFinanceExpense().getAmount(), overview.getFinanceExpense().getPercent()));
//        structure.setList(list);
        return result;
    }

    @Override
    public ExpenseTrendDTO getTrend(LocalDate date) {
        // 取过去 12 个月的数据
        String start = date.minusMonths(11).withDayOfMonth(1).format(DF);
        String end = date.format(DF);

        List<Map<String, Object>> rawList = expenseRepository.getMonthlyTrend(start, end);

        ExpenseTrendDTO trendDTO = new ExpenseTrendDTO();
        trendDTO.setMonths(rawList.stream().map(m -> m.get("MONTH_STR").toString()).collect(Collectors.toList()));

        trendDTO.setSales(rawList.stream()
                .map(m -> BigDecimal.valueOf(getDoubleValue(m, "SALES")).divide(BILLION_DIVISOR, 2, RoundingMode.HALF_UP))
                .collect(Collectors.toList()));

        trendDTO.setManagement(rawList.stream()
                .map(m -> BigDecimal.valueOf(getDoubleValue(m, "MANAGE")).divide(BILLION_DIVISOR, 2, RoundingMode.HALF_UP))
                .collect(Collectors.toList()));

        trendDTO.setFinance(rawList.stream()
                .map(m -> BigDecimal.valueOf(getDoubleValue(m, "FINANCE")).divide(BILLION_DIVISOR, 2, RoundingMode.HALF_UP))
                .collect(Collectors.toList()));

        return trendDTO;
    }

    @Override
    public List<CompanyComparisonDTO> getComparison(LocalDate date) {
        List<Map<String, Object>> rawList = expenseRepository.getCompanyComparison(date.format(MF),date.format(MF));

        List<CompanyComparisonDTO> companyComparisonDTOS = new ArrayList<>();
        rawList.forEach(map -> {
            CompanyComparisonDTO comparisonDTO = new CompanyComparisonDTO();
            comparisonDTO.setName(map.get("COMPANY_NAME").toString());
            comparisonDTO.setSales(BigDecimal.valueOf(getDoubleValue(map, "SALES")));
            comparisonDTO.setManagement(BigDecimal.valueOf(getDoubleValue(map, "MANAGE")));
            comparisonDTO.setFinance(BigDecimal.valueOf(getDoubleValue(map, "FINANCE")));
            companyComparisonDTOS.add(comparisonDTO);
        });
        return companyComparisonDTOS;
    }

    @Override
    public CompanyDetailListDTO getCompanyDetail(LocalDate date, String keyword, Integer page, Integer pageSize) {
        String queryDate = date.format(MF);
        // 如果关键字为空字符串，将其设为 null 方便 SQL 判断
        String queryKey = StringUtils.hasText(keyword) ? keyword.trim() : null;

        int offset = (page - 1) * pageSize;

        // 1. 获取分页数据
        List<Map<String, Object>> rawList = expenseRepository.getCompanyDetailsByPage(queryDate, queryKey, offset, pageSize);
        // 2. 获取总条数
        long total = expenseRepository.countCompanyDetails(queryDate, queryKey);

        CompanyDetailListDTO dto = new CompanyDetailListDTO();
        dto.setTotal(total);

        List<CompanyDetailListDTO.Item> itemList = rawList.stream().map(m -> {
            CompanyDetailListDTO.Item item = new CompanyDetailListDTO.Item();
            item.setName(m.get("COMPANY_NAME") != null ? m.get("COMPANY_NAME").toString() : "未知公司");

            // 数据转万元
            double salesExp = getDoubleValue(m, "SALES_EXP");
            double manageExp = getDoubleValue(m, "MANAGE_EXP");
            double financeExp = getDoubleValue(m, "FINANCE_EXP");
            double totalExp = salesExp + manageExp + financeExp;
            item.setSales(BigDecimal.valueOf(salesExp));
            item.setManagement(BigDecimal.valueOf(manageExp));
            item.setFinance(BigDecimal.valueOf(financeExp));
            item.setTotal(BigDecimal.valueOf(totalExp));

            // 同比
//            item.setYoy(BigDecimal.valueOf(getDoubleValue(m, "YOY_RATE")).setScale(2, RoundingMode.HALF_UP));
            return item;
        }).collect(Collectors.toList());

        dto.setList(itemList);
        return dto;
    }

    @Override
    public List<ExpenseDailyDetail> getDailyDetail(LocalDate date, String companyName) {
        List<ExpenseDailyDetail> resultList = new ArrayList<>();
        String yesterday = date.format(DF);
        expenseRepository.getDailyDetail(yesterday,companyName).forEach(map->{
            ExpenseDailyDetail expenseDailyDetail = new ExpenseDailyDetail();
            expenseDailyDetail.setCompanyName(map.get("COMPANY_NAME").toString());
            expenseDailyDetail.setTypes(map.get("TYPES").toString());
            expenseDailyDetail.setText(map.get("TEXT").toString());
            expenseDailyDetail.setAmount(BigDecimal.valueOf(getDoubleValue(map, "AMOUNT")));
            resultList.add(expenseDailyDetail);
        });


        return resultList;
    }

    @Override
    public List<BudgetExecutionDTO> getBudgetExecution(LocalDate date, String dimension) {
        List<BudgetExecutionDTO> resultList = new ArrayList<>();
        String thisMonth = date.format(MF);
        String startMonth = date.format(MF);
        if ("year".equals(dimension)){
            startMonth = thisMonth.substring(0, 4) + "-01";
        }
        Map<Object, Map<String, Object>> budgetMaps = expenseRepository.getBudget(startMonth,thisMonth).stream()
                .collect(Collectors.toMap(
                        map -> map.get("COMPANY_NAME"),
                        map -> map,
                        (existing, replacement) -> replacement
                ));
        expenseRepository.getCompanyComparison(startMonth,thisMonth).forEach(map->{
            BudgetExecutionDTO budgetExecutionDTO = new BudgetExecutionDTO();
            budgetExecutionDTO.setCompanyName(map.get("COMPANY_NAME").toString());
            budgetExecutionDTO.setSalesActual(BigDecimal.valueOf(getDoubleValue(map, "SALES")));
            budgetExecutionDTO.setMgmtActual(BigDecimal.valueOf(getDoubleValue(map, "MANAGE")));
            budgetExecutionDTO.setFinActual(BigDecimal.valueOf(getDoubleValue(map, "FINANCE")));
            Map<String, Object> budgetMap = budgetMaps.get(map.get("COMPANY_NAME"));
            budgetExecutionDTO.setSalesBudget(BigDecimal.valueOf(getDoubleValue(budgetMap, "SALES_BUDGET")));
            budgetExecutionDTO.setMgmtBudget(BigDecimal.valueOf(getDoubleValue(budgetMap, "MANAGE_BUDGET")));
            budgetExecutionDTO.setFinBudget(BigDecimal.valueOf(getDoubleValue(budgetMap, "FINANCE_BUDGET")));
            resultList.add(budgetExecutionDTO);
        });
        return resultList;
    }

    @Override
    public List<CompanyGrowthPointDTO> getCompanyGrowthData(String date) {
        // 1. 解析日期并计算 同比(去年同月) 和 环比(上个月) 的月份字符串
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate current = LocalDate.parse(date.substring(0, 7) + "-01");

        String currentMonth = current.format(df);
        String yoyMonth = current.minusYears(1).format(df);
        String momMonth = current.minusMonths(1).format(df);

        // 2. 分别获取三个月份的数据
        Map<String, BigDecimal> currentMap = convertToMap(expenseRepository.findCompanyMonthlySums(currentMonth));
        Map<String, BigDecimal> yoyMap = convertToMap(expenseRepository.findCompanyMonthlySums(yoyMonth));
        Map<String, BigDecimal> momMap = convertToMap(expenseRepository.findCompanyMonthlySums(momMonth));

        // 3. 获取所有出现的公司名称并进行逻辑组装
        Set<String> allCompanies = new HashSet<>(currentMap.keySet());

        return allCompanies.stream().map(company -> {
            BigDecimal curVal = currentMap.getOrDefault(company, BigDecimal.ZERO);
            BigDecimal yoyVal = yoyMap.getOrDefault(company, BigDecimal.ZERO);
            BigDecimal momVal = momMap.getOrDefault(company, BigDecimal.ZERO);

            return CompanyGrowthPointDTO.builder()
                    .companyName(company)
                    .currentValue(curVal.setScale(2, RoundingMode.HALF_UP))
                    .yoyValue(yoyVal.setScale(2, RoundingMode.HALF_UP))
                    .momValue(momVal.setScale(2, RoundingMode.HALF_UP))
                    .yoy(calculateGrowthRate(curVal, yoyVal))
                    .mom(calculateGrowthRate(curVal, momVal))
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 计算增长率逻辑：((本期 - 比较期) / abs(比较期)) * 100
     */
    private BigDecimal calculateGrowthRate(BigDecimal now, BigDecimal before) {
        if (before == null || before.compareTo(BigDecimal.ZERO) == 0) {
            return now.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        // 增长额 = 本期 - 比较期
        BigDecimal diff = now.subtract(before);
        // 基数取绝对值
        BigDecimal base = before.abs();

        return diff.multiply(new BigDecimal("100"))
                .divide(base, 2, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> convertToMap(List<Map<String, Object>> list) {
        return list.stream().collect(Collectors.toMap(
                m -> String.valueOf(m.get("companyName")),
                m -> m.get("totalAmount") != null ? new BigDecimal(m.get("totalAmount").toString()) : BigDecimal.ZERO
        ));
    }

    // ---------------- 私有辅助方法 ----------------

    private ExpenseOverviewDTO.MetricDetail buildMetric(Map<String, Object> raw, String curKey, String lstKey) {
        double cur = getDoubleValue(raw, curKey);
        double lst = getDoubleValue(raw, lstKey);

        ExpenseOverviewDTO.MetricDetail detail = new ExpenseOverviewDTO.MetricDetail();
        detail.setAmount(BigDecimal.valueOf(cur).divide(BILLION_DIVISOR, 2, RoundingMode.HALF_UP));
        detail.setYoyChange(BigDecimal.valueOf(MathUtil.calculateYoy(cur, lst)));
        detail.setUnit("万");
        return detail;
    }

    private double getDoubleValue(Map<String, Object> raw, String key) {
        if (raw == null || raw.get(key) == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.get(key).toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
