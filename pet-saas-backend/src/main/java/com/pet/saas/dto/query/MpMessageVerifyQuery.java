package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信服务器验证查询条件")
public class MpMessageVerifyQuery {

    @Schema(description = "微信加密签名")
    private String signature;

    @Schema(description = "时间戳")
    private String timestamp;

    @Schema(description = "随机数")
    private String nonce;

    @Schema(description = "随机字符串")
    private String echostr;
}
