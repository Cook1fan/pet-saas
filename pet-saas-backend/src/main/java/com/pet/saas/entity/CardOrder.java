package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_order")
public class CardOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long memberId;

    private Long cardRuleId;

    private Long memberCardId;

    private String orderNo;

    private String cardName;

    private Integer times;

    private BigDecimal price;

    private Integer validDays;

    private BigDecimal payAmount;

    private Integer payType;

    private Integer payStatus;

    private LocalDateTime payTime;

    private String transactionId;
}
