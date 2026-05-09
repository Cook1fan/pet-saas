package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "商品SKU保存请求")
public class GoodsSkuSaveReq {

    @Schema(description = "SKU ID（更新时必填）")
    private Long id;

    @Schema(description = "规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规格名称不能为空")
    private String specName;

    @Schema(description = "规格值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "规格值不能为空")
    private String specValue;

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @Schema(description = "成本价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成本价不能为空")
    private BigDecimal costPrice;

    @Schema(description = "是否无限库存：0-否，1-是")
    private Integer isUnlimitedStock;

    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
