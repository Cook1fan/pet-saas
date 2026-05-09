package com.pet.saas.dto.req;

import com.pet.saas.entity.PetInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "PC端会员编辑请求")
public class PcMemberUpdateReq {

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会员ID不能为空")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "标签（多个用逗号分隔）")
    private String tags;

    @Schema(description = "宠物列表（不传则不修改，传空列表则删除所有宠物）")
    private List<PetInfo> pets;
}
