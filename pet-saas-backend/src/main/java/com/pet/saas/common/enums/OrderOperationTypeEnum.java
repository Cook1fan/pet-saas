package com.pet.saas.common.enums;

import lombok.Getter;

/**
 * 订单操作类型枚举
 */
@Getter
public enum OrderOperationTypeEnum {

    CREATE(1, "创建订单"),
    PAY(2, "支付成功"),
    SHIP(3, "发货"),
    COMPLETE(4, "完成"),
    CANCEL(5, "取消"),
    REFUND(6, "退款");

    private final int code;
    private final String desc;

    OrderOperationTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderOperationTypeEnum of(int code) {
        for (OrderOperationTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
