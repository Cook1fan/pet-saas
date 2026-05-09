import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { getGoodsImageUploadPolicy, getSignUrl, type OssUploadPolicyResp } from '@/api/file'
import { useImageCompress, type CompressResult } from './useImageCompress'

/**
 * 商品图片上传 Composable
 * 封装阿里云 OSS 直传逻辑
 */
export function useImageUpload() {
  const uploading = ref(false)
  const uploadProgress = ref(0)
  const compressing = ref(false)
  const compressResult = ref<CompressResult | null>(null)
  const { compressGoodsImage, formatFileSize } = useImageCompress()

  /**
   * 上传图片到 OSS
   * @param file 图片文件
   * @param skipCompress 是否跳过压缩（默认: false）
   * @returns 图片签名 URL（用于显示）和原始 URL（用于保存）
   */
  async function uploadToOss(file: File, skipCompress = false): Promise<{ signUrl: string; originalUrl: string }> {
    uploading.value = true
    uploadProgress.value = 0
    compressResult.value = null

    console.log('========== 开始上传图片 ==========')
    console.log('文件信息:', {
      name: file.name,
      type: file.type,
      size: file.size,
      sizeMB: (file.size / 1024 / 1024).toFixed(2) + 'MB'
    })

    let uploadFile = file

    try {
      // 0. 图片压缩（如果需要）
      if (!skipCompress) {
        console.log('[0/5] 正在压缩图片...')
        compressing.value = true
        const result = await compressGoodsImage(file)
        compressResult.value = result
        uploadFile = result.compressedFile
        console.log('✅ 图片压缩成功:', {
          originalSize: formatFileSize(result.originalSize),
          compressedSize: formatFileSize(result.compressedSize),
          compressionRatio: (result.compressionRatio * 100).toFixed(1) + '%'
        })
        compressing.value = false
      }

      // 1. 获取上传凭证
      console.log('[1/5] 正在获取上传凭证...')
      uploadProgress.value = 10
      const policy = await getGoodsImageUploadPolicy({
        fileName: uploadFile.name,
        fileType: uploadFile.type,
        fileSize: uploadFile.size
      })
      console.log('✅ 获取上传凭证成功:', policy)
      uploadProgress.value = 25

      // 2. 构造 FormData 和上传 URL
      console.log('[2/5] 正在构造上传数据...')
      const formData = buildOssFormData(policy, uploadFile)
      const ossUrl = buildOssUploadUrl(policy)
      console.log('OSS 上传地址:', ossUrl)
      console.log('FormData 字段:', [...formData.keys()])

      // 3. 上传到 OSS
      console.log('[3/5] 正在上传到 OSS...')
      const response = await axios.post(ossUrl, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total) {
            const percent = Math.round((progressEvent.loaded * 50) / progressEvent.total)
            uploadProgress.value = 25 + percent
            console.log(`上传进度: ${uploadProgress.value}%`)
          }
        }
      })

      console.log('✅ OSS 上传成功!')
      console.log('响应状态:', response.status)
      console.log('响应头:', response.headers)
      console.log('原始文件 URL:', policy.fileUrl)
      uploadProgress.value = 75

      // 4. 获取签名 URL
      console.log('[4/5] 正在获取签名 URL...')
      const signUrl = await getSignUrl(policy.fileUrl)
      console.log('✅ 获取签名 URL 成功:', signUrl)

      uploadProgress.value = 100
      console.log('========== 上传完成 ==========')

      // 显示压缩结果信息
      if (compressResult.value) {
        const ratio = (compressResult.value.compressionRatio * 100).toFixed(0)
        ElMessage.success(`图片上传成功！已节省 ${ratio}% 流量`)
      } else {
        ElMessage.success('图片上传成功')
      }

      return {
        signUrl,
        originalUrl: policy.fileUrl
      }
    } catch (error: any) {
      console.error('❌ 上传失败!')
      console.error('错误详情:', error)
      console.error('错误响应:', error.response?.data)
      console.error('错误状态:', error.response?.status)
      console.error('错误头:', error.response?.headers)

      // 更友好的错误提示
      let errorMsg = '上传失败，请重试'
      if (error.response?.status === 403) {
        errorMsg = 'OSS 权限不足，请联系管理员检查配置'
      } else if (error.response?.status === 400) {
        errorMsg = '上传参数错误，请检查文件格式和大小'
      } else if (error.message) {
        errorMsg = error.message
      }

      ElMessage.error(errorMsg)
      throw error
    } finally {
      uploading.value = false
      compressing.value = false
    }
  }

  /**
   * 构建 OSS 上传 FormData
   */
  function buildOssFormData(policy: OssUploadPolicyResp, file: File): FormData {
    const formData = new FormData()
    // 注意：FormData 的顺序很重要，file 必须放在最后
    formData.append('key', policy.key)
    formData.append('policy', policy.policy)
    formData.append('OSSAccessKeyId', policy.accessKeyId)
    formData.append('success_action_status', policy.successActionStatus)
    formData.append('signature', policy.signature)
    formData.append('file', file)
    return formData
  }

  /**
   * 构建 OSS 上传 URL
   * 将 endpoint 转换为带 bucket 的格式
   */
  function buildOssUploadUrl(policy: OssUploadPolicyResp): string {
    // endpoint 格式: https://oss-cn-hangzhou.aliyuncs.com
    // 需要转换为: https://{bucket}.oss-cn-hangzhou.aliyuncs.com
    const endpointWithoutProtocol = policy.endpoint.replace(/^https?:\/\//, '')
    const url = `https://${policy.bucket}.${endpointWithoutProtocol}`
    console.log('构建的 OSS URL:', url)
    return url
  }

  /**
   * Element Plus Upload 组件的 http-request 自定义上传方法
   */
  async function handleElUpload(options: { file: File; onSuccess: (result: { signUrl: string; originalUrl: string }) => void; onError: (error: any) => void; skipCompress?: boolean }) {
    try {
      const result = await uploadToOss(options.file, options.skipCompress)
      options.onSuccess(result)
    } catch (error) {
      options.onError(error)
    }
  }

  return {
    uploading,
    uploadProgress,
    compressing,
    compressResult,
    uploadToOss,
    handleElUpload,
    formatFileSize
  }
}
