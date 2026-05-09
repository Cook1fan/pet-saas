package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "提交门店注册信息请求")
public class MpRegisterSubmitReq {

    @Schema(description = "微信授权临时token", required = true)
    @NotBlank(message = "微信授权token不能为空")
    private String mpToken;

    @Schema(description = "门店名称", required = true)
    @NotBlank(message = "门店名称不能为空")
    private String shopName;

    @Schema(description = "门店地址", required = true)
    @NotBlank(message = "门店地址不能为空")
    private String address;

    @Schema(description = "联系手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系手机号不能为空")
    private String phone;
}
