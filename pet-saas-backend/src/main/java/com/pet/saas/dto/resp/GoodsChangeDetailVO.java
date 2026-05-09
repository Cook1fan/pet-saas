package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品变更详情项
 */
@Data
@Schema(description = "商品变更详情项")
public class GoodsChangeDetailVO {

    @Schema(description = "字段名（英文）")
    private String fieldName;

    @Schema(description = "字段标签（中文）")
    private String fieldLabel;

    @Schema(description = "变更前值（格式化后）")
    private String beforeValue;

    @Schema(description = "变更后值（格式化后）")
    private String afterValue;

    @Schema(description = "SKU标识（仅SKU变更时有值，格式：规格名-规格值）")
    private String skuIdentifier;
}
