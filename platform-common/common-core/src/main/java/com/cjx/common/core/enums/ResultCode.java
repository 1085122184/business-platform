package com.cjx.common.core.enums;

import lombok.Getter;

/**
 * 统一响应结果码枚举
 * @author system
 */
@Getter
public enum ResultCode {
    // ========== 成功 ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // ========== 服务器错误 5xx ==========
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ========== 业务错误 6xx ==========
    BUSINESS_ERROR(600, "业务处理失败"),
    DATA_NOT_FOUND(601, "数据不存在"),
    DATA_ALREADY_EXISTS(602, "数据已存在"),
    DATA_INVALID(603, "数据无效"),
    OPERATION_NOT_ALLOWED(604, "操作不允许"),

    // ========== 用户相关 1xxx ==========
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    USERNAME_OR_PASSWORD_ERROR(1004, "用户名或密码错误"),
    PASSWORD_ERROR(1005, "密码错误"),
    CAPTCHA_ERROR(1006, "验证码错误"),
    CAPTCHA_EXPIRED(1007, "验证码已过期"),
    TOKEN_INVALID(1008, "Token无效"),
    TOKEN_EXPIRED(1009, "Token已过期"),

    // ========== 订单相关 2xxx ==========
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态错误"),
    ORDER_AMOUNT_ERROR(2003, "订单金额错误"),
    ORDER_EXPIRED(2004, "订单已过期"),

    // ========== 商品相关 3xxx ==========
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(3002, "商品库存不足"),
    PRODUCT_DISABLED(3003, "商品已下架"),

    // ========== 支付相关 4xxx ==========
    PAYMENT_FAILED(4001, "支付失败"),
    PAYMENT_TIMEOUT(4002, "支付超时"),
    REFUND_FAILED(4003, "退款失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
