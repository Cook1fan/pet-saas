import { ref, onMounted } from 'vue'
import Compressor from 'compressorjs'

export interface CompressResult {
  originalFile: File
  compressedFile: File
  originalSize: number
  compressedSize: number
  compressionRatio: number
}

export interface CompressOptions {
  maxWidth?: number
  maxHeight?: number
  quality?: number
  mimeType?: 'image/webp' | 'image/jpeg' | 'image/png'
}

const defaultOptions: CompressOptions = {
  maxWidth: 1920,
  maxHeight: 1920,
  quality: 0.85,
  mimeType: 'image/webp'
}

/**
 * 检查浏览器是否支持 WebP 格式
 */
function checkWebPSupport(): Promise<boolean> {
  return new Promise((resolve) => {
    const webP = new Image()
    webP.onload = () => resolve(true)
    webP.onerror = () => resolve(false)
    webP.src = 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA'
  })
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 图片压缩 composable
 */
export function useImageCompress() {
  const webPSupported = ref<boolean | null>(null)

  onMounted(async () => {
    webPSupported.value = await checkWebPSupport()
  })

  /**
   * 压缩图片
   */
  async function compressImage(
    file: File,
    options: CompressOptions = {}
  ): Promise<CompressResult> {
    const mergedOptions = { ...defaultOptions, ...options }

    // 如果浏览器不支持 WebP，使用 JPEG
    if (mergedOptions.mimeType === 'image/webp' && webPSupported.value === false) {
      mergedOptions.mimeType = 'image/jpeg'
    }

    return new Promise((resolve, reject) => {
      new Compressor(file, {
        quality: mergedOptions.quality,
        mimeType: mergedOptions.mimeType,
        maxWidth: mergedOptions.maxWidth,
        maxHeight: mergedOptions.maxHeight,
        mimeType: mergedOptions.mimeType,
        convertSize: Infinity, // 始终尝试转换为 WebP
        success: (compressedFile: File) => {
          const originalSize = file.size
          const compressedSize = compressedFile.size
          const compressionRatio = 1 - compressedSize / originalSize

          resolve({
            originalFile: file,
            compressedFile,
            originalSize,
            compressedSize,
            compressionRatio
          })
        },
        error: (error: Error) => {
          reject(error)
        }
      })
    })
  }

  /**
   * 压缩商品主图
   */
  async function compressGoodsImage(file: File): Promise<CompressResult> {
    return compressImage(file, {
      maxWidth: 1920,
      maxHeight: 1920,
      quality: 0.85
    })
  }

  return {
    webPSupported,
    compressImage,
    compressGoodsImage,
    formatFileSize
  }
}
