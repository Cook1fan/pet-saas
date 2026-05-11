package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "次卡规则保存请求")
public class CardRuleSaveReq {

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规则名称不能为空")
    private String name;

    @Schema(description = "次数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "次数不能为空")
    @DecimalMin(value = "1", message = "次数必须大于0")
    private Integer times;

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    @Schema(description = "有效期天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "有效期天数不能为空")
    @DecimalMin(value = "1", message = "有效期天数必须大于0")
    private Integer validDays;
}
