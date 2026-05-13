package com.cjx.common.core.enums;

import lombok.Getter;

/**
 * 性别枚举
 * @author system
 */
@Getter
public enum GenderEnum {
    FEMALE(0, "女"),
    MALE(1, "男"),
    UNKNOWN(2, "未知");

    private final Integer code;
    private final String desc;

    GenderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return UNKNOWN.desc;
        }
        for (GenderEnum gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender.getDesc();
            }
        }
        return UNKNOWN.desc;
    }

    /**
     * 根据code获取枚举
     */
    public static GenderEnum getByCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (GenderEnum gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
