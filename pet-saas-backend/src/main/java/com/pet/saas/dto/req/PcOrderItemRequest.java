package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "PC端订单项请求")
public class PcOrderItemRequest {

    @Schema(description = "商品SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品SKU ID不能为空")
    private Long skuId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private Integer num;
}
