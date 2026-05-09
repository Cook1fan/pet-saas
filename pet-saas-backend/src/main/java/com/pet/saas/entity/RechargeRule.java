package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recharge_rule")
public class RechargeRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String name;

    private BigDecimal rechargeAmount;

    private BigDecimal giveAmount;

    private Integer status;
}
