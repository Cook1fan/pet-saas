package com.pet.saas.common.enums;

import lombok.Getter;

/**
 * 订单来源枚举
 */
@Getter
public enum OrderSourceEnum {

    PC(1, "PC端开单收银"),
    USER_MINIAPP(2, "C端小程序"),
    MERCHANT_MINIAPP(3, "商家端");

    private final int code;
    private final String desc;

    OrderSourceEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderSourceEnum of(int code) {
        for (OrderSourceEnum source : values()) {
            if (source.code == code) {
                return source;
            }
        }
        return null;
    }
}
