package com.pet.saas.common.enums;

import lombok.Getter;

@Getter
public enum FlowTypeEnum {

    RECHARGE(1, "充值"),
    CONSUME(2, "消费"),
    REFUND(3, "退款");

    private final int code;
    private final String desc;

    FlowTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FlowTypeEnum of(int code) {
        for (FlowTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
