package com.pet.saas.common.enums;

import lombok.Getter;

/**
 * 库存变动类型枚举
 */
@Getter
public enum StockChangeTypeEnum {

    PURCHASE_IN(1, "采购入库"),
    MANUAL_IN(2, "手动入库"),
    SALE_OUT(3, "销售出库"),
    MANUAL_OUT(4, "手动出库"),
    ADJUST(5, "盘点调整"),
    RETURN_IN(6, "退货入库"),
    RESERVE(7, "预占库存"),
    RESERVE_RELEASE(8, "释放预占库存");

    private final int code;
    private final String desc;

    StockChangeTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StockChangeTypeEnum of(int code) {
        for (StockChangeTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
