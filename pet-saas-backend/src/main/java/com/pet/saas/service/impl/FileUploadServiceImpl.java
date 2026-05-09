package com.pet.saas.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.util.OssUtil;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.config.properties.OssProperties;
import com.pet.saas.dto.resp.OssUploadPolicyResp;
import com.pet.saas.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final OssProperties ossProperties;
    private final OssUtil ossUtil;

    @Override
    public OssUploadPolicyResp getGoodsImageUploadPolicy(String fileName, String fileType, Long fileSize) {
        // 1. 校验文件类型和大小
        validateFile(fileType, fileSize);

        // 2. 获取租户ID（多租户隔离）
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        if (tenantId == null) {
            throw new BusinessException("请先登录");
        }

        // 3. 生成文件key：images/{tenantId}/goods/{yyyyMMdd}/{uuid}.{ext}
        String fileKey = generateFileKey(tenantId, "goods", fileName);

        // 4. 生成Policy和签名
        OSS ossClient = ossUtil.createClient();
        try {
            long expireEndTime = System.currentTimeMillis() + ossProperties.getExpireTime() * 1000L;
            Date expiration = new Date(expireEndTime);

            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, ossProperties.getMaxSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, fileKey);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            // 5. 构建完整访问URL
            String fileUrl = ossUtil.buildFileUrl(fileKey);

            return OssUploadPolicyResp.builder()
                    .endpoint(ossProperties.getEndpoint())
                    .bucket(ossProperties.getBucket())
                    .key(fileKey)
                    .fileUrl(fileUrl)
                    .policy(encodedPolicy)
                    .signature(postSignature)
                    .accessKeyId(ossProperties.getAccessKeyId())
                    .successActionStatus("204")
                    .build();
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        String fileKey = ossUtil.extractFileKey(fileUrl);
        if (fileKey == null) {
            log.warn("无法从URL中提取fileKey: {}", fileUrl);
            return;
        }

        OSS ossClient = ossUtil.createClient();
        try {
            ossClient.deleteObject(ossProperties.getBucket(), fileKey);
            log.info("删除OSS文件成功: {}", fileKey);
        } catch (Exception e) {
            log.error("删除OSS文件失败: {}", fileKey, e);
            throw new BusinessException("删除文件失败");
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey, Integer expireSeconds) {
        return ossUtil.generatePresignedUrl(fileKey, expireSeconds);
    }

    @Override
    public String generatePresignedUrlFromFullUrl(String fileUrl, Integer expireSeconds) {
        String fileKey = ossUtil.extractFileKey(fileUrl);
        if (fileKey == null) {
            return fileUrl;
        }
        try {
            return ossUtil.generatePresignedUrl(fileKey, expireSeconds);
        } catch (Exception e) {
            log.error("生成签名URL失败，返回原URL: {}", fileUrl, e);
            return fileUrl;
        }
    }

    @Override
    public String generatePresignedUrlFromFullUrl(String fileUrl) {
        return generatePresignedUrlFromFullUrl(fileUrl, ossProperties.getExpireTime());
    }

    /**
     * 校验文件
     */
    private void validateFile(String fileType, Long fileSize) {
        if (ossProperties.getAllowedTypes() != null && !ossProperties.getAllowedTypes().isEmpty()) {
            if (!ossProperties.getAllowedTypes().contains(fileType)) {
                throw new BusinessException("不支持的文件类型，仅支持：" + String.join("、", ossProperties.getAllowedTypes()));
            }
        }

        if (fileSize > ossProperties.getMaxSize()) {
            long maxSizeMb = ossProperties.getMaxSize() / 1024 / 1024;
            throw new BusinessException("文件大小不能超过" + maxSizeMb + "MB");
        }
    }

    /**
     * 生成文件key
     */
    private String generateFileKey(Long tenantId, String bizType, String fileName) {
        String ext = getFileExtension(fileName);
        String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%s/%d/%s/%s/%s.%s",
                ossProperties.getPathPrefix(),
                tenantId,
                bizType,
                datePath,
                uuid,
                ext);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ext.matches("^(jpg|jpeg|png|webp|gif)$")) {
            return "jpg";
        }
        return ext;
    }
}
