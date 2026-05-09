package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "商品保存请求")
public class GoodsSaveReq {

    @Schema(description = "商品ID（更新时必填）")
    private Long id;

    @Schema(description = "分类ID，关联goods_category", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品名称不能为空")
    private String goodsName;

    @Schema(description = "商品主图URL")
    private String mainImage;

    @Schema(description = "是否服务类商品：0-实物商品，1-服务商品", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品类型不能为空")
    private Integer isService;

    @Schema(description = "状态：0-下架，1-上架")
    private Integer status;

    @Schema(description = "乐观锁版本号（编辑时必填）")
    private Integer version;

    @Schema(description = "SKU列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<GoodsSkuSaveReq> skuList;
}
