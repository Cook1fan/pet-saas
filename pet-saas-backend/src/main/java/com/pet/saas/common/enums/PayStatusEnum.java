package com.pet.saas.common.enums;

import lombok.Getter;

@Getter
public enum PayStatusEnum {

    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    REFUNDED(2, "已退款");

    private final int code;
    private final String desc;

    PayStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayStatusEnum of(int code) {
        for (PayStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
