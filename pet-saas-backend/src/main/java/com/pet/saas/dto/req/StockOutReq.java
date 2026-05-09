package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 出库请求
 */
@Data
@Schema(description = "出库请求")
public class StockOutReq {

    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @Schema(description = "变动类型：3-销售出库，4-手动出库，7-领用出库", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变动类型不能为空")
    private Integer type;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer num;

    @Schema(description = "关联单据类型")
    private String relatedType;

    @Schema(description = "关联单据 ID")
    private Long relatedId;

    @Schema(description = "关联单据号")
    private String relatedNo;

    @Schema(description = "备注")
    private String remark;
}
