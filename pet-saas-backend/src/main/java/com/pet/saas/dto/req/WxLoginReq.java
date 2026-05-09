package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信授权登录请求")
public class WxLoginReq {

    @Schema(description = "微信登录 code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "微信 code 不能为空")
    private String code;

    @Schema(description = "租户 ID（扫码进入时传入）")
    private Long tenantId;
}
