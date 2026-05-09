package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租户列表查询条件")
public class TenantQuery {

    @Schema(description = "门店名称（模糊搜索）")
    private String shopName;

    @Schema(description = "老板手机号")
    private String adminPhone;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
