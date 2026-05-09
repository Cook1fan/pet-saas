package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "商品分类响应")
public class GoodsCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "父分类ID，0表示一级大类")
    private Long parentId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "排序，数字越小越靠前")
    private Integer sort;

    @Schema(description = "子分类列表")
    private List<GoodsCategoryVO> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
