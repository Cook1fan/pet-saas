package com.pet.saas.common.enums;

import lombok.Getter;

/**
 * 拼团组状态枚举
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Getter
public enum ActivityGroupStatusEnum {

    GROUPING(1, "拼团中"),
    SUCCESS(2, "拼团成功"),
    FAILED(3, "拼团失败");

    private final int code;
    private final String desc;

    ActivityGroupStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ActivityGroupStatusEnum of(int code) {
        for (ActivityGroupStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
