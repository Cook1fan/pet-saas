package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_record")
public class FlowRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String flowNo;

    private Long orderId;

    private BigDecimal amount;

    private Integer type;

    private Integer payStatus;

    private String transactionId;

    private Integer reconcileStatus;
}
