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
@Schema(description = "提交门店注册信息响应")
public class MpRegisterSubmitRespVO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "登录token")
    private String token;

    @Schema(description = "管理员账号（用于PC登录）")
    private String username;

    @Schema(description = "初始密码（仅首次返回，请妥善保存）")
    private String password;
}
