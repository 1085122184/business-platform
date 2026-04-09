package com.cjx.common.core.constant;
/**
 * 通用常量
 * @author system
 */
public interface CommonConstant {
    /** 成功标记 */
    Integer SUCCESS = 200;

    /** 失败标记 */
    Integer FAIL = 500;

    /** 状态:正常 */
    Integer STATUS_NORMAL = 1;

    /** 状态:禁用 */
    Integer STATUS_DISABLE = 0;

    /** 删除标记:未删除 */
    Integer DEL_FLAG_NORMAL = 0;

    /** 删除标记:已删除 */
    Integer DEL_FLAG_DELETE = 1;

    /** 默认分页大小 */
    Integer DEFAULT_PAGE_SIZE = 10;

    /** 最大分页大小 */
    Integer MAX_PAGE_SIZE = 100;
}
