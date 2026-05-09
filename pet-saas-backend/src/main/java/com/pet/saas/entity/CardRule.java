package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_rule")
public class CardRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String name;

    private Integer times;

    private BigDecimal price;

    private Integer validDays;

    private Integer status;
}
