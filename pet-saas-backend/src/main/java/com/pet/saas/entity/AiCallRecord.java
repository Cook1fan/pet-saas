package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_call_record")
public class AiCallRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private String type;

    private String prompt;

    private String result;

    private Integer costTimes;
}
