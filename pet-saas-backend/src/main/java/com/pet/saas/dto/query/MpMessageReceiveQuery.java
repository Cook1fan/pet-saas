package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信消息接收查询条件")
public class MpMessageReceiveQuery {

    @Schema(description = "微信加密签名")
    private String signature;

    @Schema(description = "时间戳")
    private String timestamp;

    @Schema(description = "随机数")
    private String nonce;

    @Schema(description = "用户openid")
    private String openid;

    @Schema(description = "加密类型")
    private String encryptType;

    @Schema(description = "消息签名")
    private String msgSignature;
}
