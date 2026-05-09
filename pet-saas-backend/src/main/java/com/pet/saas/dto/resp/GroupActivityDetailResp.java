package com.pet.saas.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动详情响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class GroupActivityDetailResp {

    private Long id;

    private String title;

    private Long goodsId;

    private String goodsName;

    private String goodsImage;

    private Long skuId;

    private String specName;

    private String specValue;

    private BigDecimal price;

    private BigDecimal originPrice;

    private Integer groupCount;

    private Integer groupValidHours;

    private Integer stock;

    private Integer soldStock;

    private Integer limitNum;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String qrCodeUrl;

    private ActivityStat stat;

    @Data
    public static class ActivityStat {
        private Long totalOrderCount;
        private Long successGroupCount;
        private Long failedGroupCount;
        private Long ongoingGroupCount;
        private BigDecimal totalGMV;
    }
}
