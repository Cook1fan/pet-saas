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
@Schema(description = "已绑定门店响应")
public class UserShopVO {

    @Schema(description = "租户 ID")
    private Long tenantId;

    @Schema(description = "门店名称")
    private String shopName;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "logo URL")
    private String logo;

    @Schema(description = "是否当前选中门店")
    private Boolean isCurrent;

    @Schema(description = "绑定时间")
    private LocalDateTime bindTime;
}
