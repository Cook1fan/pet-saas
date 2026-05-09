package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_group")
public class ActivityGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long activityId;

    private String groupNo;

    private Long leaderMemberId;

    private Integer currentNum;

    private Integer targetNum;

    private Integer status;

    private LocalDateTime expireTime;

    private LocalDateTime successTime;
}
