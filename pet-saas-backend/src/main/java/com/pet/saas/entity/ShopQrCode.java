package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺二维码表
 */
@Data
@TableName("shop_qr_code")
public class ShopQrCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 二维码类型：1-店铺码，2-商品码，3-活动码
     */
    private Integer qrType;

    /**
     * 二维码名称/描述
     */
    private String qrName;

    /**
     * 场景参数（如 tenant_123）
     */
    private String scene;

    /**
     * 二维码图片 URL
     */
    private String qrUrl;

    /**
     * 微信返回的 ticket
     */
    private String qrTicket;

    /**
     * 过期时间（永久二维码则为空）
     */
    private LocalDateTime expireTime;

    /**
     * 是否默认二维码：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 扫码次数
     */
    private Integer scanCount;

    /**
     * 创建人 ID
     */
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 修改人 ID
     */
    private Long updateUser;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}