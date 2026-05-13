package com.cjx.common.core.utils;

import cn.hutool.core.util.IdUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务工具类
 *
 * 与Hutool区别：
 * - 提供业务特定的工具方法
 * - 如订单号生成、流水号生成等业务场景
 *
 * @author system
 */
public class BizUtil {
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成订单号
     * 格式：ORD + yyyyMMddHHmmss + 6位随机数
     * 示例：ORD20240120153045A1B2C3
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String random = IdUtil.randomUUID().substring(0, 6).toUpperCase();
        return "ORD" + timestamp + random;
    }

    /**
     * 生成支付流水号
     * 格式：PAY + yyyyMMddHHmmss + 8位随机数
     */
    public static String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String random = String.format("%08d", ThreadLocalRandom.current().nextInt(100000000));
        return "PAY" + timestamp + random;
    }

    /**
     * 生成退款流水号
     * 格式：REF + yyyyMMddHHmmss + 8位随机数
     */
    public static String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String random = String.format("%08d", ThreadLocalRandom.current().nextInt(100000000));
        return "REF" + timestamp + random;
    }

    /**
     * 生成用户邀请码
     * 6位大写字母+数字组合
     */
    public static String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去除易混淆字符
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            code.append(chars.charAt(index));
        }
        return code.toString();
    }

    /**
     * 生成短信验证码
     * @param length 验证码长度
     */
    public static String generateSmsCode(int length) {
        if (length <= 0 || length > 10) {
            length = 6;
        }
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(ThreadLocalRandom.current().nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成6位短信验证码
     */
    public static String generateSmsCode() {
        return generateSmsCode(6);
    }

    /**
     * 计算两个金额的差值（单位：分）
     * 避免浮点数精度问题
     */
    public static long calculateAmountDiff(long amount1, long amount2) {
        return Math.abs(amount1 - amount2);
    }

    /**
     * 判断金额是否相等（允许1分的误差）
     */
    public static boolean isAmountEqual(long amount1, long amount2) {
        return calculateAmountDiff(amount1, amount2) <= 1;
    }

    /**
     * 格式化金额（分转元，保留2位小数）
     */
    public static String formatAmount(long amountInCent) {
        return String.format("%.2f", amountInCent / 100.0);
    }

    /**
     * 隐藏部分字符串（通用）
     * @param str 原字符串
     * @param startLen 开始保留长度
     * @param endLen 结束保留长度
     */
    public static String hideString(String str, int startLen, int endLen) {
        if (str == null || str.length() <= startLen + endLen) {
            return str;
        }
        String start = str.substring(0, startLen);
        String end = str.substring(str.length() - endLen);
        int hideLen = str.length() - startLen - endLen;
        return start + "*".repeat(hideLen) + end;
    }
}
