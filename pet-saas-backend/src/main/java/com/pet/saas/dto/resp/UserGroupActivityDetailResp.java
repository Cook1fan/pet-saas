package com.pet.saas.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端拼团活动详情响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class UserGroupActivityDetailResp {

    private Long id;

    private String title;

    private Long goodsId;

    private String goodsName;

    private String goodsImage;

    private List<String> goodsImages;

    private BigDecimal price;

    private BigDecimal originPrice;

    private Integer groupCount;

    private Integer remainStock;

    private Integer limitNum;

    private Long endTime;

    private Integer userLimitRemain;

    private List<OngoingGroup> ongoingGroups;

    @Data
    public static class OngoingGroup {
        private Long groupId;
        private String groupNo;
        private Integer currentNum;
        private Integer targetNum;
        private String leaderAvatar;
        private String leaderName;
        private Long remainTime;
        private String remainTimeDesc;
    }
}
