package com.cjx.common.core.constant;

/**
 * 通用常量
 * 包含系统中常用的状态码、标记等常量定义
 *
 * @author system
 */
public final class CommonConstant {

    private CommonConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /** 成功标记 */
    public static final Integer SUCCESS = 200;

    /** 失败标记 */
    public static final Integer FAIL = 500;

    /** 状态:正常 */
    public static final Integer STATUS_NORMAL = 1;

    /** 状态:禁用 */
    public static final Integer STATUS_DISABLE = 0;

    /** 删除标记:未删除 */
    public static final Integer DEL_FLAG_NORMAL = 0;

    /** 删除标记:已删除 */
    public static final Integer DEL_FLAG_DELETE = 1;

    /** 默认分页大小 */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /** 最大分页大小 */
    public static final Integer MAX_PAGE_SIZE = 100;
}
