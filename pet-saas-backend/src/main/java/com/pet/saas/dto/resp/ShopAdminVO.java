package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "门店管理员信息响应")
public class ShopAdminVO {

    @Schema(description = "管理员ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "账号")
    private String username;

    @Schema(description = "角色：shop_admin-管理员，shop_staff-店员")
    private String role;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
