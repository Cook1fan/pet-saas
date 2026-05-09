package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_card")
public class MemberCard extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long memberId;

    private Long cardRuleId;

    private Integer remainTimes;

    private LocalDateTime expireTime;
}
