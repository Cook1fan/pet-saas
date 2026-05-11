package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "通过核销码核销请求")
public class VerifyByCodeReq {

    @Schema(description = "核销码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "核销码不能为空")
    @Pattern(regexp = "\\d{6}", message = "核销码必须是6位数字")
    private String verifyCode;
}
