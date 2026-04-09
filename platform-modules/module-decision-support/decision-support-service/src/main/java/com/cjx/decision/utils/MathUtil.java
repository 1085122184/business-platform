package com.cjx.decision.utils;

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
}
