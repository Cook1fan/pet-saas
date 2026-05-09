package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_order")
public class ActivityOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long activityId;

    private Long orderId;

    private Long memberId;

    private Long groupId;

    private Integer status;
}
