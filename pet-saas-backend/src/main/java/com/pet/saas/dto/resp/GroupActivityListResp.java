package com.pet.saas.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动列表响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class GroupActivityListResp {

    private Long id;

    private String title;

    private String goodsName;

    private String goodsImage;

    private BigDecimal price;

    private BigDecimal originPrice;

    private Integer groupCount;

    private Integer stock;

    private Integer soldStock;

    private Integer limitNum;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusDesc;

    private Integer successCount;

    private Integer orderCount;
}
