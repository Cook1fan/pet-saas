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
@Schema(description = "微信登录响应")
public class WxLoginRespVO {

    @Schema(description = "登录 Token")
    private String token;

    @Schema(description = "会员信息")
    private MemberVO member;

    @Schema(description = "是否新注册会员")
    private Boolean isNewMember;

    @Schema(description = "微信 openid")
    private String openid;
}
