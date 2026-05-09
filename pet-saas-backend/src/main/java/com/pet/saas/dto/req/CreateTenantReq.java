package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建租户请求")
public class CreateTenantReq {

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "门店名称不能为空")
    private String shopName;

    @Schema(description = "老板手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "老板手机号不能为空")
    private String adminPhone;

    @Schema(description = "门店地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "门店地址不能为空")
    private String address;
}
