package com.pet.saas.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatusEnum {

    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REFUNDED(5, "已退款");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatusEnum of(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查状态流转是否合法
     *
     * @param targetStatus 目标状态
     * @return 是否可以流转
     */
    public boolean canTransitionTo(OrderStatusEnum targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        return switch (this) {
            case UNPAID -> targetStatus == PAID || targetStatus == SHIPPED || targetStatus == CANCELLED;
            case PAID -> targetStatus == SHIPPED || targetStatus == REFUNDED || targetStatus == COMPLETED;
            case SHIPPED -> targetStatus == COMPLETED || targetStatus == REFUNDED;
            case COMPLETED -> targetStatus == REFUNDED;
            default -> false;
        };
    }
}
