package com.pet.saas.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VerifyCodeStatusEnum {

    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    INVALID(2, "已失效");

    private final Integer code;
    private final String desc;
}
