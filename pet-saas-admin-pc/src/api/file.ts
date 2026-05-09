import request from '@/utils/request'

// ==================== 类型定义 ====================

export interface OssUploadPolicyReq {
  fileName: string
  fileType: string
  fileSize: number
}

export interface OssUploadPolicyResp {
  endpoint: string
  bucket: string
  key: string
  fileUrl: string
  policy: string
  signature: string
  accessKeyId: string
  successActionRedirect?: string
  successActionStatus: string
}

export interface FileSignUrlReq {
  fileUrl: string
}

// ==================== 接口函数 ====================

/**
 * 获取商品图片上传凭证
 */
export function getGoodsImageUploadPolicy(data: OssUploadPolicyReq) {
  return request.post<OssUploadPolicyResp>('/pc/file/upload-policy/goods-image', data)
}

/**
 * 获取文件签名URL
 * 将原始OSS URL转换为带签名的临时访问URL
 */
export function getSignUrl(fileUrl: string) {
  return request.post<string>('/pc/file/sign-url', { fileUrl })
}

/**
 * 删除文件
 */
export function deleteFile(fileUrl: string) {
  return request.delete('/pc/file', { params: { fileUrl } })
}
