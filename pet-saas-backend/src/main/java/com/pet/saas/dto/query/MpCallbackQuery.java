package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信授权回调查询条件")
public class MpCallbackQuery {

    @Schema(description = "微信授权code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "微信授权code不能为空")
    private String code;

    @Schema(description = "状态参数（授权后跳转的页面地址）")
    private String state;
}
