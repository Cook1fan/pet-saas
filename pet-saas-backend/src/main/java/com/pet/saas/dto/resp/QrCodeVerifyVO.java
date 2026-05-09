package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 二维码验证响应 VO
 */
@Data
@Schema(description = "二维码验证响应")
public class QrCodeVerifyVO {

    @Schema(description = "是否有效")
    private Boolean valid;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "租户Logo")
    private String tenantLogo;

    @Schema(description = "是否已绑定（当前用户是否已绑定该店铺）")
    private Boolean isBound;
}