package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "核销码响应")
public class VerifyCodeVO {

    @Schema(description = "核销码，6位数字")
    private String verifyCode;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "次卡名称")
    private String cardName;

    @Schema(description = "剩余次数")
    private Integer remainTimes;
}
