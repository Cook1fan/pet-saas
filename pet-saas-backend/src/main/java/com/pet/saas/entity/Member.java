package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member")
public class Member extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String phone;

    private String name;

    private String tags;

    private String openid;

    private String avatar;

    /**
     * 注册来源：1-扫码注册，2-店员添加，3-推荐注册
     */
    private Integer registerSource;

    /**
     * 来源租户 ID（扫码注册时记录）
     */
    private Long fromTenantId;
}
