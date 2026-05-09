package com.pet.saas.dto.resp;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团组列表响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class ActivityGroupListResp {

    private Long id;

    private String groupNo;

    private String leaderMemberName;

    private String leaderMemberPhone;

    private Integer currentNum;

    private Integer targetNum;

    private Integer status;

    private String statusDesc;

    private LocalDateTime expireTime;

    private LocalDateTime successTime;

    private LocalDateTime createTime;

    private List<GroupMember> members;

    @Data
    public static class GroupMember {
        private String memberName;
        private String memberPhone;
        private Boolean isLeader;
        private LocalDateTime joinTime;
    }
}
