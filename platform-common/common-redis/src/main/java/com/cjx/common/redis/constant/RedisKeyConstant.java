package com.cjx.common.redis.constant;

/**
 * Redis Key 常量
 *
 * 命名规范:
 * 业务模块:功能:ID
 * 例: user:token:123456
 *
 * @author system
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== 用户相关 ==========

    /** 用户Token: user:token:{token} */
    public static final String USER_TOKEN = "user:token:";

    /** 用户信息缓存: user:info:{userId} */
    public static final String USER_INFO = "user:info:";

    /** 用户登录失败次数: user:login:fail:{username} */
    public static final String USER_LOGIN_FAIL = "user:login:fail:";

    /** 用户在线状态: user:online:{userId} */
    public static final String USER_ONLINE = "user:online:";

    // ========== 验证码相关 ==========

    /** 图片验证码: captcha:image:{key} */
    public static final String CAPTCHA_IMAGE = "captcha:image:";

    /** 短信验证码: captcha:sms:{mobile} */
    public static final String CAPTCHA_SMS = "captcha:sms:";

    /** 邮箱验证码: captcha:email:{email} */
    public static final String CAPTCHA_EMAIL = "captcha:email:";

    // ========== 限流相关 ==========

    /** 接口限流: rate:limit:{api}:{ip} */
    public static final String RATE_LIMIT_API = "rate:limit:api:";

    /** 用户操作限流: rate:limit:user:{userId}:{action} */
    public static final String RATE_LIMIT_USER = "rate:limit:user:";

    /** IP限流: rate:limit:ip:{ip} */
    public static final String RATE_LIMIT_IP = "rate:limit:ip:";

    // ========== 分布式锁 ==========

    /** 分布式锁: lock:{resource} */
    public static final String DISTRIBUTED_LOCK = "lock:";

    /** 订单锁: lock:order:{orderId} */
    public static final String LOCK_ORDER = "lock:order:";

    /** 库存锁: lock:stock:{productId} */
    public static final String LOCK_STOCK = "lock:stock:";

    // ========== 缓存相关 ==========

    /** 缓存空值(防穿透): cache:null:{key} */
    public static final String CACHE_NULL = "cache:null:";

    /** 热点数据: cache:hot:{key} */
    public static final String CACHE_HOT = "cache:hot:";

    // ========== 业务数据 ==========

    /** 订单缓存: order:info:{orderId} */
    public static final String ORDER_INFO = "order:info:";

    /** 商品库存: product:stock:{productId} */
    public static final String PRODUCT_STOCK = "product:stock:";

    /** 商品详情: product:info:{productId} */
    public static final String PRODUCT_INFO = "product:info:";

    // ========== 过期时间(秒) ==========

    /** Token过期时间: 7天 */
    public static final Long TOKEN_EXPIRE = 7 * 24 * 60 * 60L;

    /** 用户信息缓存: 1小时 */
    public static final Long USER_INFO_EXPIRE = 60 * 60L;

    /** 验证码过期时间: 5分钟 */
    public static final Long CAPTCHA_EXPIRE = 5 * 60L;

    /** 短信验证码过期时间: 10分钟 */
    public static final Long SMS_EXPIRE = 10 * 60L;

    /** 限流时间窗口: 1分钟 */
    public static final Long RATE_LIMIT_WINDOW = 60L;

    /** 分布式锁默认过期: 30秒 */
    public static final Long LOCK_EXPIRE = 30L;

    /** 缓存空值过期: 5分钟 */
    public static final Long NULL_EXPIRE = 5 * 60L;
}
