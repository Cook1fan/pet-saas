package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信授权链接生成查询条件")
public class MpAuthorizeQuery {

    @Schema(description = "授权后跳转的页面地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "授权后跳转地址不能为空")
    private String redirectUrl;
}
