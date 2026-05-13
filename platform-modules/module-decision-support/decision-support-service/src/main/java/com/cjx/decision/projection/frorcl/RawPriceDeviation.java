package com.cjx.decision.projection.frorcl;

/**
 * 价格偏差
 * @author CUIJIXU
 */
public interface RawPriceDeviation {
    String getProductName();
    String getProductCode();
    String getRegion();
    Double getAvgPrice7d();
    Double getTodayPrice();
    Double getDeviationAmt();
    Double getDeviationPct(); // 前端按百分比数值处理，如 -11.11
}
