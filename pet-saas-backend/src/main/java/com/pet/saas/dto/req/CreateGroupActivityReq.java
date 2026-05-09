package com.pet.saas.dto.req;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建拼团活动请求 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class CreateGroupActivityReq {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "活动价格不能为空")
    @Positive(message = "活动价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "原价不能为空")
    @Positive(message = "原价必须大于0")
    private BigDecimal originPrice;

    @NotNull(message = "成团人数不能为空")
    @Positive(message = "成团人数必须大于0")
    private Integer groupCount;

    @NotNull(message = "拼团有效期不能为空")
    @Positive(message = "拼团有效期必须大于0")
    private Integer groupValidHours;

    @NotNull(message = "活动库存不能为空")
    @Positive(message = "活动库存必须大于0")
    private Integer stock;

    @NotNull(message = "限购数量不能为空")
    @Positive(message = "限购数量必须大于0")
    private Integer limitNum;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
