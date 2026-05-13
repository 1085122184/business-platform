package com.cjx.common.core.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 数据脱敏工具类
 *
 * 与Hutool区别：
 * - Hutool的DesensitizedUtil功能较少
 * - 本类提供更丰富的脱敏规则和自定义能力
 *
 * @author system
 */
public class SensitiveUtil {
    /**
     * 手机号脱敏
     * 13812345678 -> 138****5678
     */
    public static String phone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证号脱敏
     * 110101199001011234 -> 110101********1234
     */
    public static String idCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return idCard;
        }
        if (idCard.length() == 15) {
            return idCard.replaceAll("(\\d{6})\\d{6}(\\d{3})", "$1******$2");
        } else if (idCard.length() == 18) {
            return idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
        }
        return idCard;
    }

    /**
     * 银行卡号脱敏
     * 6222600123456789012 -> 6222 **** **** 9012
     */
    public static String bankCard(String cardNo) {
        if (StrUtil.isBlank(cardNo) || cardNo.length() < 12) {
            return cardNo;
        }
        return cardNo.substring(0, 4) + " **** **** " + cardNo.substring(cardNo.length() - 4);
    }

    /**
     * 邮箱脱敏
     * example@qq.com -> ex***@qq.com
     */
    public static String email(String email) {
        if (StrUtil.isBlank(email)) {
            return email;
        }
        int index = email.indexOf("@");
        if (index <= 1) {
            return email;
        }
        String prefix = email.substring(0, index);
        String suffix = email.substring(index);

        if (prefix.length() <= 2) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }

    /**
     * 姓名脱敏
     * 张三 -> 张*
     * 欧阳娜娜 -> 欧**娜
     */
    public static String name(String name) {
        if (StrUtil.isBlank(name) || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(name.length() - 1));
        return sb.toString();
    }

    /**
     * 地址脱敏
     * 保留省市区，详细地址脱敏
     * 北京市朝阳区建国路1号 -> 北京市朝阳区****
     */
    public static String address(String address) {
        if (StrUtil.isBlank(address) || address.length() < 10) {
            return address;
        }
        // 简单处理：保留前9个字符
        return address.substring(0, 9) + "****";
    }

    /**
     * 车牌号脱敏
     * 京A12345 -> 京A****5
     */
    public static String carLicense(String license) {
        if (StrUtil.isBlank(license) || license.length() < 7) {
            return license;
        }
        return license.substring(0, 3) + "****" + license.charAt(license.length() - 1);
    }

    /**
     * 自定义脱敏
     * @param str 原始字符串
     * @param start 保留开始位置（从0开始）
     * @param end 保留结束位置
     * @param mask 脱敏字符
     */
    public static String custom(String str, int start, int end, String mask) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        if (start < 0 || end > str.length() || start >= end) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, start));
        for (int i = start; i < end; i++) {
            sb.append(mask);
        }
        if (end < str.length()) {
            sb.append(str.substring(end));
        }
        return sb.toString();
    }
}
