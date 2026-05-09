package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "获取文件签名URL请求")
public class FileSignUrlReq {

    @Schema(description = "原始文件URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;
}
