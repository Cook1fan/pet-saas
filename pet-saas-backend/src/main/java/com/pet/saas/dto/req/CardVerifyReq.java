package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "次卡核销请求")
public class CardVerifyReq {

    @Schema(description = "核销码，6位数字", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "核销码不能为空")
    private String verifyCode;
}
