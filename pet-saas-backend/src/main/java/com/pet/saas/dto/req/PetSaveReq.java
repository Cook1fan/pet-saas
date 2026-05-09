package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "保存宠物档案请求")
public class PetSaveReq {

    @Schema(description = "宠物ID（编辑时必填）")
    private Long id;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @Schema(description = "宠物名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "宠物名称不能为空")
    private String name;

    @Schema(description = "品种")
    private String breed;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "性别：1-公，2-母")
    private Integer gender;

    @Schema(description = "疫苗时间")
    private LocalDate vaccineTime;

    @Schema(description = "驱虫时间")
    private LocalDate dewormTime;

    @Schema(description = "洗澡时间")
    private LocalDate washTime;

    @Schema(description = "下次提醒时间")
    private LocalDateTime nextRemindTime;
}
