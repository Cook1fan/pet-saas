package com.pet.saas.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.pet.saas.config.properties.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;

/**
 * OSS 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OssProperties ossProperties;

    /**
     * 创建 OSS 客户端
     */
    public OSS createClient() {
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }

    /**
     * 生成签名URL
     */
    public String generatePresignedUrl(String fileKey, Integer expireSeconds) {
        OSS ossClient = createClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            URL url = ossClient.generatePresignedUrl(ossProperties.getBucket(), fileKey, expiration);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成签名URL（使用默认过期时间）
     */
    public String generatePresignedUrl(String fileKey) {
        return generatePresignedUrl(fileKey, ossProperties.getExpireTime());
    }

    /**
     * 从完整URL中提取fileKey
     */
    public String extractFileKey(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            log.warn("从URL中提取fileKey失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 构建文件访问URL（优先使用CDN）
     */
    public String buildFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        if (fileKey.startsWith("http://") || fileKey.startsWith("https://")) {
            return fileKey;
        }
        if (ossProperties.getCdnDomain() != null && !ossProperties.getCdnDomain().isEmpty()) {
            return ossProperties.getCdnDomain() + "/" + fileKey;
        }
        return "https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint().replace("https://", "") + "/" + fileKey;
    }
}
