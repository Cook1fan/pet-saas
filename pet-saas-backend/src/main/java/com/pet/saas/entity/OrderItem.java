package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long orderId;

    private Long goodsId;

    private Long skuId;

    private String barcode;

    private String goodsName;

    private Integer num;

    private BigDecimal price;

    /**
     * 成本价（快照）
     */
    private BigDecimal costPrice;
}
