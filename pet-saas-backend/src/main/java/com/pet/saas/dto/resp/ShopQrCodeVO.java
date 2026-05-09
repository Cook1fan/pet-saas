package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺二维码响应 VO
 */
@Data
@Schema(description = "店铺二维码响应")
public class ShopQrCodeVO {

    @Schema(description = "二维码ID")
    private Long qrCodeId;

    @Schema(description = "二维码名称")
    private String qrName;

    @Schema(description = "二维码图片URL")
    private String qrUrl;

    @Schema(description = "二维码类型：1-店铺码，2-商品码，3-活动码")
    private Integer qrType;

    @Schema(description = "扫码次数")
    private Integer scanCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}