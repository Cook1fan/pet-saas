package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "生成核销码请求")
public class GenerateVerifyCodeReq {

    @Schema(description = "会员次卡 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会员次卡 ID 不能为空")
    private Long memberCardId;
}
