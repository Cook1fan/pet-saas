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
@Schema(description = "微信公众号商家登录响应")
public class MpShopLoginRespVO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "登录token")
    private String token;
}
