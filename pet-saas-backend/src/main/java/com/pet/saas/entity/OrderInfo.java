package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class OrderInfo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String orderNo;

    private Long memberId;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer payType;

    private Integer payStatus;

    /**
     * 订单来源：1-PC端开单收银，2-C端小程序，3-商家端
     */
    private Integer orderSource;

    /**
     * 订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消，5-已退款
     */
    private Integer orderStatus;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

    private LocalDateTime payTime;

    private String transactionId;
}
