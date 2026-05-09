package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_platform_admin")
public class SysPlatformAdmin extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String role;
}
