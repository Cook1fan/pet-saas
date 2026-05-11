package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "购买次卡请求")
public class CardPurchaseReq {

    @Schema(description = "次卡规则ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "次卡规则ID不能为空")
    private Long cardRuleId;
}
