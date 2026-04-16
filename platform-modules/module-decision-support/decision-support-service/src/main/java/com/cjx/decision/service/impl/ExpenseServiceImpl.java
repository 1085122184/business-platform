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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final BigDecimal BILLION_DIVISOR = new BigDecimal("100000000"); // 亿元单位转换器

    @Override
    public ExpenseOverviewDTO getOverview(LocalDate date) {
        String curr = date.format(DF);
        String last = date.minusYears(1).format(DF);
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
        total.setUnit("亿");

        // 生成文字描述 (例如：同比上升 ¥1.25亿)
        double diff = (curTotal - lstTotal) / 1_0000_0000.0;
        String desc = diff > 0 ? String.format("同比上升 ¥%.2f亿", diff) : String.format("同比下降 ¥%.2f亿", Math.abs(diff));
        total.setYoyChangeText(desc);

        dto.setTotalExpense(total);
        dto.setSalesExpense(sales);
        dto.setManagementExpense(manage);
        dto.setFinanceExpense(finance);
        return dto;
    }

    @Override
    public ExpenseStructureDTO getStructure(LocalDate date) {
        // 复用总览数据来计算占比
        ExpenseOverviewDTO overview = this.getOverview(date);
        ExpenseStructureDTO structure = new ExpenseStructureDTO();
        List<ExpenseStructureDTO.StructureItem> list = new ArrayList<>();

        list.add(new ExpenseStructureDTO.StructureItem("销售费用", overview.getSalesExpense().getAmount(), overview.getSalesExpense().getPercent()));
        list.add(new ExpenseStructureDTO.StructureItem("管理费用", overview.getManagementExpense().getAmount(), overview.getManagementExpense().getPercent()));
        list.add(new ExpenseStructureDTO.StructureItem("财务费用", overview.getFinanceExpense().getAmount(), overview.getFinanceExpense().getPercent()));

        structure.setList(list);
        return structure;
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
    public CompanyComparisonDTO getComparison(LocalDate date) {
        List<Map<String, Object>> rawList = expenseRepository.getCompanyComparison(date.format(DF));

        CompanyComparisonDTO comparisonDTO = new CompanyComparisonDTO();
        comparisonDTO.setCompany(rawList.stream().map(m -> m.get("COMPANY_NAME").toString()).collect(Collectors.toList()));

        // 转换为万元展示
        BigDecimal tenThousand = new BigDecimal("10000");
        comparisonDTO.setSales(rawList.stream().map(m -> BigDecimal.valueOf(getDoubleValue(m, "SALES")).divide(tenThousand, 2, RoundingMode.HALF_UP)).collect(Collectors.toList()));
        comparisonDTO.setManagement(rawList.stream().map(m -> BigDecimal.valueOf(getDoubleValue(m, "MANAGE")).divide(tenThousand, 2, RoundingMode.HALF_UP)).collect(Collectors.toList()));
        comparisonDTO.setFinance(rawList.stream().map(m -> BigDecimal.valueOf(getDoubleValue(m, "FINANCE")).divide(tenThousand, 2, RoundingMode.HALF_UP)).collect(Collectors.toList()));

        return comparisonDTO;
    }

    @Override
    public CompanyDetailListDTO getCompanyDetail(LocalDate date, String keyword, Integer page, Integer pageSize) {
        String queryDate = date.format(DF);
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
            BigDecimal tenThousand = new BigDecimal("10000");
            item.setSales(BigDecimal.valueOf(getDoubleValue(m, "SALES_EXP")).divide(tenThousand, 2, RoundingMode.HALF_UP));
            item.setManagement(BigDecimal.valueOf(getDoubleValue(m, "MANAGE_EXP")).divide(tenThousand, 2, RoundingMode.HALF_UP));
            item.setFinance(BigDecimal.valueOf(getDoubleValue(m, "FINANCE_EXP")).divide(tenThousand, 2, RoundingMode.HALF_UP));
            item.setTotal(BigDecimal.valueOf(getDoubleValue(m, "TOTAL_EXP")).divide(tenThousand, 2, RoundingMode.HALF_UP));

            // 同比
            item.setYoy(BigDecimal.valueOf(getDoubleValue(m, "YOY_RATE")).setScale(2, RoundingMode.HALF_UP));
            return item;
        }).collect(Collectors.toList());

        dto.setList(itemList);
        return dto;
    }

    // ---------------- 私有辅助方法 ----------------

    private ExpenseOverviewDTO.MetricDetail buildMetric(Map<String, Object> raw, String curKey, String lstKey) {
        double cur = getDoubleValue(raw, curKey);
        double lst = getDoubleValue(raw, lstKey);

        ExpenseOverviewDTO.MetricDetail detail = new ExpenseOverviewDTO.MetricDetail();
        detail.setAmount(BigDecimal.valueOf(cur).divide(BILLION_DIVISOR, 2, RoundingMode.HALF_UP));
        detail.setYoyChange(BigDecimal.valueOf(MathUtil.calculateYoy(cur, lst)));
        detail.setUnit("亿");
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