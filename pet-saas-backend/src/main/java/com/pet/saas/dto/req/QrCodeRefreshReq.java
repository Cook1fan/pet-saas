package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 刷新二维码请求
 */
@Data
@Schema(description = "刷新二维码请求")
public class QrCodeRefreshReq {

    @Schema(description = "二维码ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "二维码ID不能为空")
    private Long qrCodeId;
}