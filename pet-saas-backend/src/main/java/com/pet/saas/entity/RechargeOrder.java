package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recharge_order")
public class RechargeOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long memberId;

    private Long rechargeRuleId;

    private String orderNo;

    private BigDecimal rechargeAmount;

    private BigDecimal giveAmount;

    private BigDecimal payAmount;

    private Integer payType;

    private Integer payStatus;

    private LocalDateTime payTime;

    private String transactionId;
}
