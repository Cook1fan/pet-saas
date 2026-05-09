package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("verify_code_record")
public class VerifyCodeRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 核销码（6位数字）
     */
    private String verifyCode;

    /**
     * 会员次卡 ID
     */
    private Long memberCardId;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态：0-未使用，1-已使用，2-已失效
     */
    private Integer status;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 核销人 ID（商家管理员/店员 ID）
     */
    private Long verifyUserId;
}
