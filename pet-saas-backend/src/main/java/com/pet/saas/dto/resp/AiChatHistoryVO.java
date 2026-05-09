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
@Schema(description = "AI 历史对话响应")
public class AiChatHistoryVO {

    @Schema(description = "记录 ID")
    private Long id;

    @Schema(description = "用户问题")
    private String question;

    @Schema(description = "AI 回答")
    private String answer;

    @Schema(description = "对话时间")
    private LocalDateTime createTime;
}
