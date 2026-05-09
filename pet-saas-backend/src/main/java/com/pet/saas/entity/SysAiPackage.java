package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_package")
public class SysAiPackage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String name;

    private Integer times;

    private BigDecimal price;

    private Integer status;
}
