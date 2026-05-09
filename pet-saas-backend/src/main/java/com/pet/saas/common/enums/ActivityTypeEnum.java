package com.pet.saas.common.enums;

import lombok.Getter;

@Getter
public enum ActivityTypeEnum {

    GROUP_BUY(1, "拼团"),
    FLASH_SALE(2, "秒杀");

    private final int code;
    private final String desc;

    ActivityTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ActivityTypeEnum of(int code) {
        for (ActivityTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
