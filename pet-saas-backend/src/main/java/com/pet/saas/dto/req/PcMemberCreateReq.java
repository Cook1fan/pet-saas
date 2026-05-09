package com.pet.saas.dto.req;

import com.pet.saas.entity.PetInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "PC端会员录入请求")
public class PcMemberCreateReq {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "标签（多个用逗号分隔）")
    private String tags;

    @Schema(description = "宠物列表")
    private List<PetInfo> pets;
}
