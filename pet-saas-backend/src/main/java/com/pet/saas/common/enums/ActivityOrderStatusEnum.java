package com.pet.saas.common.enums;

import lombok.Getter;

@Getter
public enum ActivityOrderStatusEnum {

    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    GROUP_SUCCESS(2, "拼团成功"),
    GROUP_FAILED(3, "拼团失败");

    private final int code;
    private final String desc;

    ActivityOrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ActivityOrderStatusEnum of(int code) {
        for (ActivityOrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
