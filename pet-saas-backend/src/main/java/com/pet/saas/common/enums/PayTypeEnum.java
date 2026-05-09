package com.pet.saas.common.enums;

import lombok.Getter;

@Getter
public enum PayTypeEnum {

    WECHAT(1, "微信支付"),
    CASH(2, "现金"),
    RECHARGE(3, "储值"),
    CARD(4, "次卡");

    private final int code;
    private final String desc;

    PayTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayTypeEnum of(int code) {
        for (PayTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
