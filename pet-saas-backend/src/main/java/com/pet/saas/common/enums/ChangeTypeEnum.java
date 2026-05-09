package com.pet.saas.common.enums;

import lombok.Getter;

/**
 * 变更类型枚举
 */
@Getter
public enum ChangeTypeEnum {

    CREATE(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除");

    private final Integer code;
    private final String desc;

    ChangeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
