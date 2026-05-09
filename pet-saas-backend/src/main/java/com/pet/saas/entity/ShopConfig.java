package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_config")
public class ShopConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String shopName;

    private String address;

    private String phone;

    private String businessHours;

    private String logo;

    private String payConfig;
}
