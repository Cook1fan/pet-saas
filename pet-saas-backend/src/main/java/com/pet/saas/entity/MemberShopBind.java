package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("member_shop_bind")
public class MemberShopBind implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信 openid
     */
    private String openid;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 会员 ID（该门店下的会员）
     */
    private Long memberId;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastVisitTime;

    /**
     * 创建时间（绑定时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 绑定来源：1-扫码注册，2-店员添加，3-推荐注册
     */
    private Integer bindSource;

    /**
     * 推荐人会员 ID
     */
    private Long referrerMemberId;
}
