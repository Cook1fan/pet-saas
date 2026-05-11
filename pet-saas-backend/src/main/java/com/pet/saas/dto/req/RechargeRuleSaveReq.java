package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "储值规则保存请求")
public class RechargeRuleSaveReq {

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规则名称不能为空")
    private String name;

    @Schema(description = "储值金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "储值金额不能为空")
    @DecimalMin(value = "0.01", message = "储值金额必须大于0")
    private BigDecimal rechargeAmount;

    @Schema(description = "赠送金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "赠送金额不能为空")
    @DecimalMin(value = "0", message = "赠送金额不能为负")
    private BigDecimal giveAmount;
}
