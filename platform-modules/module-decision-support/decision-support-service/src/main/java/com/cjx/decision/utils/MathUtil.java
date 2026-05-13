package com.cjx.decision.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    /**
     * 计算两个数组的皮尔逊相关系数
     * @param xArray 例如：过去 15 天的每日销量
     * @param yArray 例如：过去 15 天的每日单价
     * @return 相关系数 (-1.0 到 1.0 之间)
     */
    public static double getPearsonCorrelation(double[] xArray, double[] yArray) {
        if (xArray.length != yArray.length || xArray.length == 0) return 0.0;

        int n = xArray.length;
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0;
        double sumX2 = 0.0, sumY2 = 0.0;

        for (int i = 0; i < n; i++) {
            sumX += xArray[i];
            sumY += yArray[i];
            sumXY += xArray[i] * yArray[i];
            sumX2 += xArray[i] * xArray[i];
            sumY2 += yArray[i] * yArray[i];
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0) return 0.0; // 防止除以 0
        return numerator / denominator;
    }

    /**
     * 计算同比/环比变化率 (%)
     * * @param current 本期数值
     * @param last    同期/上期数值
     * @return 变化率百分比（保留两位小数），若同期为 0，则返回 0.0 或 100.0
     */
    public static double calculateYoy(double current, double last) {
        if (last == 0.0) {
            return current > 0 ? 100.0 : 0.0;
        }
        BigDecimal curDec = BigDecimal.valueOf(current);
        BigDecimal lastDec = BigDecimal.valueOf(last);

        // (current - last) / last * 100
        return curDec.subtract(lastDec)
                .divide(lastDec, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 安全计算占比 (%)
     *
     * @param part  部分数值
     * @param total 总数值
     * @return 占比百分比（保留两位小数）
     */
    public static double calculatePercent(double part, double total) {
        if (total == 0.0) {
            return 0.0;
        }
        return BigDecimal.valueOf(part)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
