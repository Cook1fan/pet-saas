package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "切换门店请求")
public class ShopSwitchReq {

    @Schema(description = "租户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租户 ID 不能为空")
    private Long tenantId;
}
