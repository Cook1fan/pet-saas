package com.pet.saas.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /**
     * OSS endpoint
     */
    private String endpoint;

    /**
     * Bucket名称
     */
    private String bucket;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * CDN加速域名
     */
    private String cdnDomain;

    /**
     * 路径前缀
     */
    private String pathPrefix = "images";

    /**
     * 最大文件大小（字节），默认5MB
     */
    private Long maxSize = 5242880L;

    /**
     * 允许的文件类型
     */
    private List<String> allowedTypes;

    /**
     * 签名有效期（秒），默认1小时
     */
    private Integer expireTime = 3600;
}
