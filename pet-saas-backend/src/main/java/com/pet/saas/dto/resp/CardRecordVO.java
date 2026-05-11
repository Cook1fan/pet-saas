package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "次卡记录响应")
public class CardRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "次卡名称")
    private String cardName;

    @Schema(description = "类型：1-购买，2-核销")
    private Integer type;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "核销次数")
    private Integer verifyTimes;

    @Schema(description = "变动前剩余次数")
    private Integer remainTimesBefore;

    @Schema(description = "变动后剩余次数")
    private Integer remainTimesAfter;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
