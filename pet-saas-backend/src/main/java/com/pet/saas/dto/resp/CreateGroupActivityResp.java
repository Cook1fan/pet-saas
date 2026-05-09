package com.pet.saas.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建拼团活动响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupActivityResp {

    private Long activityId;

    private String qrCodeUrl;
}
