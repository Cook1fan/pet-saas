package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建租户响应")
public class CreateTenantRespVO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户名（手机号）")
    private String username;
}
