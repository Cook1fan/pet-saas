package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "营销活动响应")
public class ActivityInfoVO {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "活动类型：1-拼团，2-秒杀")
    private Integer type;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "商品/服务ID")
    private Long goodsId;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "活动价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originPrice;

    @Schema(description = "成团人数（拼团活动专用）")
    private Integer groupCount;

    @Schema(description = "拼团有效期（小时）")
    private Integer groupValidHours;

    @Schema(description = "活动库存")
    private Integer stock;

    @Schema(description = "限购数量")
    private Integer limitNum;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-未开始，1-进行中，2-已结束")
    private Integer status;

    @Schema(description = "活动海报URL")
    private String qrCodeUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
