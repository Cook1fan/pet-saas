package com.pet.saas.dto.req;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 发起拼团请求 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class LaunchGroupReq {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "购买数量不能为空")
    private Integer num;
}
