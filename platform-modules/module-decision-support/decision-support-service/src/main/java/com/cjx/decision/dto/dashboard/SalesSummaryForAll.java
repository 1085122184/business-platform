package com.cjx.decision.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;


/**
 * @author cuijixu
 */
@Data
public class SalesSummaryForAll {

    private BigDecimal value;
    private BigDecimal target;


    private Double totalSales; // 当日总销量
    private Double totalAmount; // 当日总金额

    private Double totalSalesMonth; // 累计总销量
    private Double totalAmountMonth; // 累计总金额

    private Double totalCountBudget;//本月总销量预算

    private Double totalAmountBudget;//本月总销售额预算

    private String companyName;//公司名称
    private Double collection;//回款
}
