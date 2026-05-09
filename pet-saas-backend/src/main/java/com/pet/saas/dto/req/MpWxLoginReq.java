package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "微信公众号登录请求")
public class MpWxLoginReq {

    @Schema(description = "微信授权临时token", required = true)
    @NotBlank(message = "微信授权token不能为空")
    private String mpToken;
}
