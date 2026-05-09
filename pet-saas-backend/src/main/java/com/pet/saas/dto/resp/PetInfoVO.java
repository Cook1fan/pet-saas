package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "宠物档案响应")
public class PetInfoVO {

    @Schema(description = "宠物ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "宠物名")
    private String name;

    @Schema(description = "品种")
    private String breed;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "性别：1-公，2-母")
    private Integer gender;

    @Schema(description = "疫苗日期")
    private LocalDate vaccineTime;

    @Schema(description = "驱虫日期")
    private LocalDate dewormTime;

    @Schema(description = "洗护日期")
    private LocalDate washTime;

    @Schema(description = "下次提醒时间")
    private LocalDateTime nextRemindTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
