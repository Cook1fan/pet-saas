package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "租户信息响应")
public class SysTenantVO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "门店名称")
    private String shopName;

    @Schema(description = "老板手机号")
    private String adminPhone;

    @Schema(description = "门店地址")
    private String address;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
