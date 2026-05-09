package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "平台管理员信息响应")
public class PlatformAdminVO {

    @Schema(description = "管理员ID")
    private Long id;

    @Schema(description = "账号")
    private String username;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
