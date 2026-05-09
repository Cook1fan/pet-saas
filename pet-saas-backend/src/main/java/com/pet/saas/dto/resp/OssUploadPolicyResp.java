package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OSS上传凭证响应")
public class OssUploadPolicyResp {

    @Schema(description = "OSS访问域名")
    private String endpoint;

    @Schema(description = "Bucket名称")
    private String bucket;

    @Schema(description = "文件存储路径（key）")
    private String key;

    @Schema(description = "完整文件访问URL")
    private String fileUrl;

    @Schema(description = "Policy")
    private String policy;

    @Schema(description = "签名")
    private String signature;

    @Schema(description = "AccessKey ID")
    private String accessKeyId;

    @Schema(description = "上传成功后返回的状态码，204表示无内容返回")
    private String successActionStatus;
}
