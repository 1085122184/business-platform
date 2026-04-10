package com.cjx.decision.projection.frorcl;

import java.math.BigDecimal;

/**
 * 卡片指标
 * @author CUIJIXU
 */
public interface SalesSummary {
    Double getTotalSales(); // 对应 SQL 中的 总销量
    Double getTotalAmount(); // 对应 SQL 中的 总金额

    BigDecimal getPrice();

    Double getTotalCountBudget();//总销量预算

    Double getTotalAmountBudget();//总销售额预算

    String getCompanyName();//公司名称

    String getProductName();//产品

    String getProductCode();//产品
    Double getCollection();//回款
    String getLatestDate();

    String getRegion();

    Integer getAmountRatio();
    Integer getSalesRatio();
    BigDecimal  getTotalOrder();
    BigDecimal  getClosedOrder();
    BigDecimal  getOpenOrder();

    String getCustomer();

}
