package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pet_info")
public class PetInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long memberId;

    private String name;

    private String breed;

    private LocalDate birthday;

    private Integer gender;

    private LocalDate vaccineTime;

    private LocalDate dewormTime;

    private LocalDate washTime;

    private LocalDateTime nextRemindTime;
}
