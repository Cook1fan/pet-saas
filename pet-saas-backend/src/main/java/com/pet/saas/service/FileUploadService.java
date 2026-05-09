package com.pet.saas.service;

import com.pet.saas.dto.resp.OssUploadPolicyResp;

/**
 * 文件上传服务
 */
public interface FileUploadService {

    /**
     * 获取商品图片上传凭证（后端签名模式）
     *
     * @param fileName 原始文件名
     * @param fileType 文件MIME类型
     * @param fileSize 文件大小（字节）
     * @return 上传凭证
     */
    OssUploadPolicyResp getGoodsImageUploadPolicy(String fileName, String fileType, Long fileSize);

    /**
     * 删除文件
     *
     * @param fileUrl 文件完整URL
     */
    void deleteFile(String fileUrl);

    /**
     * 生成文件访问URL（私有Bucket时使用）
     *
     * @param fileKey 文件key
     * @param expireSeconds 过期时间（秒）
     * @return 签名URL
     */
    String generatePresignedUrl(String fileKey, Integer expireSeconds);

    /**
     * 从完整URL生成签名访问URL（私有Bucket时使用）
     *
     * @param fileUrl 完整文件URL
     * @param expireSeconds 过期时间（秒）
     * @return 签名URL，如果URL为空或不合法则返回原URL
     */
    String generatePresignedUrlFromFullUrl(String fileUrl, Integer expireSeconds);

    /**
     * 从完整URL生成签名访问URL（使用默认过期时间）
     *
     * @param fileUrl 完整文件URL
     * @return 签名URL
     */
    String generatePresignedUrlFromFullUrl(String fileUrl);
}
