# 商品图片上传阿里云OSS设计方案

**文档导航：** [返回总览](./00-总览.md)

---

## 一、方案概述

### 1.1 背景
在商品录入时需要上传商品主图，图片需要持久化存储并可通过URL访问。本方案采用「前端直传 + 后端签名」的模式，将图片上传到阿里云OSS。

### 1.2 核心设计原则
- **安全性**：使用STS临时授权或后端签名，不暴露OSS AccessKey
- **高性能**：前端直传OSS，不经过后端服务器，减少带宽消耗
- **可扩展**：支持多种图片类型，预留缩略图、水印等扩展能力
- **易维护**：统一的文件上传服务，后续其他模块（如宠物头像、活动海报）可复用

---

## 二、上传流程设计

### 2.1 整体架构图

```
┌─────────┐    1. 请求上传凭证     ┌──────────┐
│  前端   │ ─────────────────────> │  后端    │
│         │                        │  服务    │
│         │    2. 返回上传凭证     │          │
│         │ <───────────────────── │          │
└────┬────┘                        └──────────┘
     │
     │ 3. 直传文件到OSS
     │
     ▼
┌─────────────────────────────────┐
│     阿里云 OSS                    │
│  (pet-saas-images bucket)        │
└─────────────────────────────────┘
```

### 2.2 详细流程

#### 方案A：后端签名模式（推荐，更简单）

1. **前端请求上传参数**
   - 前端调用后端接口，传入文件名、文件类型
   - 后端验证权限，生成OSS上传签名和参数
   - 返回：bucket、endpoint、key、policy、signature、OSSAccessKeyId

2. **前端直传OSS**
   - 前端使用FormData，携带上述参数，直接POST到OSS
   - OSS验证签名后保存文件
   - 返回204 No Content表示上传成功

3. **前端获取文件URL**
   - 上传成功后，前端拼接bucket + endpoint + key得到完整URL
   - 将URL提交给后端保存到商品表的main_image字段

#### 方案B：STS临时授权模式（更安全，适合多租户）

1. **前端请求STS Token**
   - 前端调用后端接口
   - 后端调用阿里云STS服务，获取临时AccessKeyId、AccessKeySecret、SecurityToken
   - 返回临时凭证和上传路径

2. **前端使用OSS SDK直传**
   - 前端使用ali-oss SDK，使用临时凭证初始化
   - 直传文件到指定路径

3. **保存URL**
   - 同方案A

---

## 三、技术实现方案

### 3.1 依赖引入

在`pom.xml`中添加阿里云OSS SDK：

```xml
<!-- 阿里云OSS SDK -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.4</version>
</dependency>
```

### 3.2 配置文件

在`application.yml`中添加OSS配置：

```yaml
# 阿里云OSS配置
aliyun:
  oss:
    endpoint: https://oss-cn-hangzhou.aliyuncs.com  # OSS外网访问域名
    bucket: pet-saas-images                            # Bucket名称
    access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID:}       # AccessKey ID（从环境变量读取）
    access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET:}  # AccessKey Secret
    # 文件路径前缀，按租户隔离：images/{tenantId}/goods/
    path-prefix: images
    # 图片大小限制：5MB
    max-size: 5242880
    # 允许的图片类型
    allowed-types:
      - image/jpeg
      - image/jpg
      - image/png
      - image/webp
    # 签名有效期（秒）
    expire-time: 3600
```

### 3.3 配置类

创建`OssProperties.java`配置类：

```java
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
```

### 3.4 OSS服务层

#### 3.4.1 文件上传服务接口

```java
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
}
```

#### 3.4.2 响应DTO

```java
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

    @Schema(description = "上传成功后的重定向地址（可选）")
    private String successActionRedirect;

    @Schema(description = "上传成功后返回的状态码，204表示无内容返回")
    private String successActionStatus;
}
```

#### 3.4.3 服务实现类

```java
package com.pet.saas.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.config.properties.OssProperties;
import com.pet.saas.dto.resp.OssUploadPolicyResp;
import com.pet.saas.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final OssProperties ossProperties;

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
        OSS ossClient = createOssClient();
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
            String fileUrl = String.format("https://%s.%s/%s",
                    ossProperties.getBucket(),
                    ossProperties.getEndpoint().replace("https://", ""),
                    fileKey);

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

        // 从URL中提取fileKey
        String fileKey = extractFileKey(fileUrl);
        if (fileKey == null) {
            log.warn("无法从URL中提取fileKey: {}", fileUrl);
            return;
        }

        OSS ossClient = createOssClient();
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
        OSS ossClient = createOssClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            URL url = ossClient.generatePresignedUrl(ossProperties.getBucket(), fileKey, expiration);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 校验文件
     */
    private void validateFile(String fileType, Long fileSize) {
        // 校验文件类型
        if (ossProperties.getAllowedTypes() != null && !ossProperties.getAllowedTypes().isEmpty()) {
            if (!ossProperties.getAllowedTypes().contains(fileType)) {
                throw new BusinessException("不支持的文件类型，仅支持：" + String.join("、", ossProperties.getAllowedTypes()));
            }
        }

        // 校验文件大小
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
        // 只允许安全的扩展名
        if (!ext.matches("^(jpg|jpeg|png|webp|gif)$")) {
            return "jpg";
        }
        return ext;
    }

    /**
     * 从URL中提取fileKey
     */
    private String extractFileKey(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 创建OSS客户端
     */
    private OSS createOssClient() {
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }
}
```

### 3.5 Controller层

```java
package com.pet.saas.controller.pc;

import com.pet.saas.common.R;
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
}
```

请求DTO：

```java
package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "获取OSS上传凭证请求")
public class OssUploadPolicyReq {

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件MIME类型，如image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;
}
```

---

## 四、前端实现示例

### 4.1 获取上传凭证并上传

```javascript
// 1. 先调用后端接口获取上传凭证
async function uploadGoodsImage(file) {
    // 获取上传凭证
    const policyRes = await axios.post('/api/pc/file/upload-policy/goods-image', {
        fileName: file.name,
        fileType: file.type,
        fileSize: file.size
    });

    const policy = policyRes.data.data;

    // 2. 构造FormData直传OSS
    const formData = new FormData();
    formData.append('key', policy.key);
    formData.append('policy', policy.policy);
    formData.append('OSSAccessKeyId', policy.accessKeyId);
    formData.append('success_action_status', '204');
    formData.append('signature', policy.signature);
    formData.append('file', file);

    // 3. 上传到OSS
    await axios.post(policy.endpoint.replace('https://', 'https://' + policy.bucket + '.'), formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

    // 4. 返回文件URL，用于保存到商品
    return policy.fileUrl;
}

// 使用示例
const fileInput = document.getElementById('goods-image-input');
fileInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (file) {
        const imageUrl = await uploadGoodsImage(file);
        // 将imageUrl赋值给表单的mainImage字段
        document.getElementById('mainImage').value = imageUrl;
        // 预览图片
        document.getElementById('image-preview').src = imageUrl;
    }
});
```

### 4.2 商品保存流程

```javascript
// 商品保存时，直接使用已上传的图片URL
async function saveGoods(goodsData) {
    // goodsData.mainImage 已经是完整的OSS URL
    await axios.post('/api/pc/goods/save', goodsData);
}
```

---

## 五、OSS Bucket配置建议

### 5.1 Bucket基本配置

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| 地域 | 华东1（杭州） | 选择靠近服务器的地域，降低延迟 |
| 存储类型 | 标准存储 | 图片访问频繁，使用标准存储 |
| 读写权限 | 公共读 | 图片需要公开访问，设置公共读 |
| 版本控制 | 关闭 | 不需要历史版本 |
| 日志记录 | 开启 | 便于问题排查 |
| 服务端加密 | 关闭 | 图片非敏感数据 |

### 5.2 CORS配置

需要配置CORS允许前端跨域上传：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<CORSConfiguration>
  <CORSRule>
    <AllowedOrigin>*</AllowedOrigin>
    <AllowedMethod>POST</AllowedMethod>
    <AllowedMethod>GET</AllowedMethod>
    <AllowedMethod>HEAD</AllowedMethod>
    <AllowedHeader>*</AllowedHeader>
    <ExposeHeader>ETag</ExposeHeader>
    <ExposeHeader>x-oss-request-id</ExposeHeader>
    <MaxAgeSeconds>3600</MaxAgeSeconds>
  </CORSRule>
</CORSConfiguration>
```

### 5.3 生命周期规则

建议配置生命周期规则，自动清理临时文件或历史文件：

- 规则名称：清理30天前的已删除文件
- 应用范围：整个Bucket
- 策略：删除30天前的删除标记

---

## 六、文件命名与目录结构

### 6.1 目录结构

按租户、业务类型、日期组织目录，便于管理：

```
images/
├── {tenantId}/              # 租户ID，多租户隔离
│   ├── goods/               # 商品图片
│   │   ├── 20260326/
│   │   │   ├── uuid1.jpg
│   │   │   └── uuid2.png
│   │   └── 20260327/
│   │       └── uuid3.webp
│   ├── pet/                 # 宠物头像
│   ├── member/              # 会员头像
│   └── activity/            # 活动海报
└── {tenantId2}/
    └── goods/
```

### 6.2 文件名规则

- 使用UUID生成文件名，避免重名
- 保留原始文件扩展名
- 格式：`{uuid}.{ext}`

---

## 七、安全考虑

### 7.1 已有的安全措施

1. **AccessKey不暴露**：使用后端签名，前端无法获取AccessKey Secret
2. **文件类型校验**：后端校验允许的MIME类型和扩展名
3. **文件大小限制**：限制最大5MB，防止恶意上传大文件
4. **Policy限制**：上传Policy限制了key前缀和文件大小
5. **多租户隔离**：不同租户的文件存储在不同目录
6. **签名有效期**：Policy有时效性，过期失效

### 7.2 额外安全建议

1. **防盗链**：配置OSS Referer白名单，防止图片被盗用
2. **图片压缩**：前端上传前压缩图片，减小文件大小
3. **内容审核**：接入阿里云内容安全，审核图片内容
4. **权限控制**：敏感图片使用私有Bucket + 签名URL访问

---

## 八、后续扩展

### 8.1 图片处理

可以使用OSS图片处理服务，实现：
- 缩略图：`?x-oss-process=image/resize,m_lfit,h_200,w_200`
- 水印：`?x-oss-process=image/watermark,...`
- 格式转换：`?x-oss-process=image/format,webp`
- 质量压缩：`?x-oss-process=image/quality,q_80`

### 8.2 批量上传

后续可扩展支持：
- 商品轮播图批量上传
- Excel批量导入商品时批量上传图片

### 8.3 图片审核

接入阿里云内容安全：
- 图片鉴黄
- 图片涉政检测
- 图片广告检测

---

## 九、实施步骤

1. **配置阿里云OSS**
   - 创建Bucket
   - 配置CORS
   - 获取AccessKey

2. **后端开发**
   - 引入OSS SDK依赖
   - 编写配置类
   - 编写FileUploadService
   - 编写FileUploadController
   - 单元测试

3. **前端开发**
   - 图片选择组件
   - 获取上传凭证
   - 直传OSS
   - 图片预览
   - 集成到商品表单

4. **联调测试**
   - 上传功能测试
   - 删除功能测试
   - 多租户隔离测试
   - 边界条件测试

---

## 十、常见问题

### Q1: 为什么不直接传后端再转发OSS？
A: 直传OSS可以减少后端带宽消耗，提升上传速度，降低服务器负载。

### Q2: 前端如何知道上传成功？
A: OSS返回204状态码表示上传成功，前端可以监听状态码判断。

### Q3: 上传失败怎么办？
A: 前端捕获错误，提示用户重新上传。可以添加重试机制。

### Q4: 旧图片怎么处理？
A: 商品更新主图时，后端可以异步删除旧图片；或者定期清理无引用的图片。

### Q5: 图片加载慢怎么办？
A: 可以开通OSS CDN加速，提升图片访问速度。