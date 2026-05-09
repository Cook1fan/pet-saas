package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 盘点调整请求
 */
@Data
@Schema(description = "盘点调整请求")
public class StockAdjustReq {

    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @Schema(description = "盘点后库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "盘点后库存不能为空")
    private Integer targetStock;

    @Schema(description = "备注")
    private String remark;
}
