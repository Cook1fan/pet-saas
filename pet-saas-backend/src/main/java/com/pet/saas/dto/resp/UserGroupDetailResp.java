package com.pet.saas.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端拼团组详情响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class UserGroupDetailResp {

    private Long groupId;

    private String groupNo;

    private Long activityId;

    private String title;

    private String goodsImage;

    private String goodsName;

    private BigDecimal price;

    private BigDecimal originPrice;

    private Integer currentNum;

    private Integer targetNum;

    private Integer status;

    private Long expireTime;

    private Long remainTime;

    private String remainTimeDesc;

    private Boolean canJoin;

    private List<GroupMember> members;

    private List<MissingMember> membersMissing;

    @Data
    public static class GroupMember {
        private Long memberId;
        private String memberName;
        private String memberAvatar;
        private Boolean isLeader;
        private Long joinTime;
    }

    @Data
    public static class MissingMember {
        private Integer index;
        private Boolean placeholder;
    }
}
