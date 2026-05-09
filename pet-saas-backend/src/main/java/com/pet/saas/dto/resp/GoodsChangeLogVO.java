package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品变更日志响应
 */
@Data
@Schema(description = "商品变更日志响应")
public class GoodsChangeLogVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "数据类型：1-goods商品，2-goods_sku商品规格")
    private Integer dataType;

    @Schema(description = "数据 ID")
    private Long dataId;

    @Schema(description = "变更类型：1-新增，2-修改，3-删除")
    private Integer changeType;

    @Deprecated
    @Schema(description = "变更字段名（已废弃）")
    private String fieldName;

    @Schema(description = "变更前值（JSON格式，仅包含变更字段）")
    private String beforeValue;

    @Schema(description = "变更后值（JSON格式，仅包含变更字段）")
    private String afterValue;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "操作人 ID")
    private Long createUser;

    @Deprecated
    @Schema(description = "格式化后的变更详情列表（已废弃，请使用 goodsChanges 和 skuChanges）")
    private List<GoodsChangeDetailVO> changeDetails;

    @Schema(description = "第一维度：商品属性变更列表")
    private List<GoodsChangeDetailVO> goodsChanges;

    @Schema(description = "第二维度：SKU变更Map，key为SKU ID，value为该SKU的所有字段变更")
    private Map<Long, List<GoodsChangeDetailVO>> skuChanges;
}
