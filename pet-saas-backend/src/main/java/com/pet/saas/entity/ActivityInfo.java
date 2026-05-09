package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_info")
public class ActivityInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Integer type;

    private String title;

    private Long goodsId;

    private Long skuId;

    private BigDecimal price;

    private BigDecimal originPrice;

    private Integer groupCount;

    private Integer groupValidHours;

    private Integer stock;

    private Integer limitNum;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String qrCodeUrl;
}
