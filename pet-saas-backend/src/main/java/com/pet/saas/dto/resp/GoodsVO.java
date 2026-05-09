package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "商品信息响应")
public class GoodsVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "分类ID，关联goods_category")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品主图URL")
    private String mainImage;

    @Schema(description = "是否服务类商品：0-实物商品，1-服务商品")
    private Integer isService;

    @Schema(description = "状态：0-下架，1-上架")
    private Integer status;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "SKU列表")
    private List<GoodsSkuVO> skuList;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
