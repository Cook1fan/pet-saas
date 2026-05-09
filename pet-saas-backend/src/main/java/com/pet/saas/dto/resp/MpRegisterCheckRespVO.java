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
@Schema(description = "微信注册预检查响应")
public class MpRegisterCheckRespVO {

    @Schema(description = "是否已注册")
    private Boolean registered;

    @Schema(description = "租户ID（已注册时返回）")
    private Long tenantId;
}
