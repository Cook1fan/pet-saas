package com.pet.saas.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端拼团活动列表响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class UserGroupActivityListResp {

    private Long id;

    private String title;

    private String goodsImage;

    private String goodsName;

    private BigDecimal price;

    private BigDecimal originPrice;

    private Integer groupCount;

    private Integer remainStock;

    private Long endTime;

    private List<HotGroup> hotGroups;

    @Data
    public static class HotGroup {
        private Long groupId;
        private Integer currentNum;
        private Integer targetNum;
        private String leaderAvatar;
        private String leaderName;
        private Long remainTime;
    }
}
