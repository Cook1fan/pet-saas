package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recharge_consume_record")
public class RechargeConsumeRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long memberId;

    private Long memberAccountId;

    private Long orderId;

    private Integer type;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String remark;
}
