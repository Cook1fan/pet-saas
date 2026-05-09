package com.pet.saas.controller.pc;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.FileSignUrlReq;
import com.pet.saas.dto.req.OssUploadPolicyReq;
import com.pet.saas.dto.resp.OssUploadPolicyResp;
import com.pet.saas.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文件上传接口", description = "PC端-文件上传相关接口")
@RestController
@RequestMapping("/api/pc/file")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "获取商品图片上传凭证", description = "获取商品主图上传到OSS所需的签名凭证")
    @PostMapping("/upload-policy/goods-image")
    public R<OssUploadPolicyResp> getGoodsImageUploadPolicy(@Valid @RequestBody OssUploadPolicyReq req) {
        OssUploadPolicyResp resp = fileUploadService.getGoodsImageUploadPolicy(
                req.getFileName(),
                req.getFileType(),
                req.getFileSize()
        );
        return R.ok(resp);
    }

    @Operation(summary = "删除文件", description = "删除OSS上的文件")
    @DeleteMapping
    public R<Void> deleteFile(@RequestParam String fileUrl) {
        fileUploadService.deleteFile(fileUrl);
        return R.ok();
    }

    @Operation(summary = "获取文件签名URL", description = "将原始OSS URL转换为带签名的临时访问URL，用于预览图片")
    @PostMapping("/sign-url")
    public R<String> signUrl(@Valid @RequestBody FileSignUrlReq req) {
        String signedUrl = fileUploadService.generatePresignedUrlFromFullUrl(req.getFileUrl());
        return R.ok(signedUrl);
    }
}
