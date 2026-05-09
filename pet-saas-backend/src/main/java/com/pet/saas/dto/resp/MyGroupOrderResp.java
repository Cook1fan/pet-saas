package com.pet.saas.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的拼团订单响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class MyGroupOrderResp {

    private Long id;

    private String orderNo;

    private Long activityId;

    private String activityTitle;

    private String goodsImage;

    private String goodsName;

    private BigDecimal price;

    private Integer num;

    private BigDecimal payAmount;

    private Long groupId;

    private Integer groupStatus;

    private String groupStatusDesc;

    private Boolean isLeader;

    private Integer currentNum;

    private Integer targetNum;

    private Long remainTime;

    private LocalDateTime createTime;

    private String shareUrl;
}
