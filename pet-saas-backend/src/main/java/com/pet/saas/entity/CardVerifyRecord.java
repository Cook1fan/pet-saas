package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_verify_record")
public class CardVerifyRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private Long memberId;

    private Long memberCardId;

    private Long cardRuleId;

    private Long orderId;

    private Long verifyCodeRecordId;

    private String cardName;

    private Integer verifyTimes;

    private Integer remainTimesBefore;

    private Integer remainTimesAfter;

    private String remark;
}
