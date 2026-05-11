package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "购买储值请求")
public class RechargePurchaseReq {

    @Schema(description = "储值规则ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "储值规则ID不能为空")
    private Long rechargeRuleId;
}
