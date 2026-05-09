package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_admin")
public class ShopAdmin extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String username;

    private String password;

    private String role;

    private Integer status;
}
