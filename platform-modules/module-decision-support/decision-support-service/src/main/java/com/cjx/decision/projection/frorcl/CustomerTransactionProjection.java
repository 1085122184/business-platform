package com.cjx.decision.projection.frorcl;

/**
 * 价格偏差明细
 * @author CUIJIXU
 */
public interface CustomerTransactionProjection {
    String getCustomer(); // 客户名称
    Double getVolume();   // 提货量（吨）
    Double getPrice();    // 实际成交单价（元）
}
