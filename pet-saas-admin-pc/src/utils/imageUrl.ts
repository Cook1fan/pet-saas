/**
 * 图片 URL 处理工具
 * 用于生成不同规格的 OSS 图片 URL
 * 注意：保留原有签名参数，只添加或替换 x-oss-process 参数
 */

/**
 * 添加或替换 OSS 处理参数
 * 保留原有查询参数（签名等），只处理 x-oss-process
 */
function addOssProcessParams(url: string, processParams: string): string {
  if (!url) return ''

  try {
    const urlObj = new URL(url)

    // 检查是否已有 x-oss-process 参数
    const existingProcess = urlObj.searchParams.get('x-oss-process')

    if (existingProcess) {
      // 如果已有，替换它（保留其他参数）
      urlObj.searchParams.set('x-oss-process', processParams)
    } else {
      // 如果没有，添加它
      urlObj.searchParams.append('x-oss-process', processParams)
    }

    return urlObj.toString()
  } catch (e) {
    // 如果 URL 解析失败（可能是相对路径或不合法 URL），回退到简单拼接
    console.warn('URL 解析失败，使用回退方案:', url)
    const queryIndex = url.indexOf('?')
    if (queryIndex > 0) {
      // 检查是否已有 x-oss-process
      if (url.includes('x-oss-process=')) {
        // 替换已有的 x-oss-process
        return url.replace(/x-oss-process=[^&]*/, `x-oss-process=${encodeURIComponent(processParams)}`)
      } else {
        // 添加新的 x-oss-process
        return `${url}&x-oss-process=${encodeURIComponent(processParams)}`
      }
    } else {
      return `${url}?x-oss-process=${encodeURIComponent(processParams)}`
    }
  }
}

/**
 * 获取缩略图 URL
 * 规格: 200x200 填充, 80% 质量, WebP
 */
export function getThumbUrl(url: string | undefined | null): string {
  if (!url) return ''
  const processParams = 'image/resize,m_fill,w_200,h_200/quality,q_80/format,webp'
  return addOssProcessParams(url, processParams)
}

/**
 * 获取小图 URL
 * 规格: 最大边 400px, 85% 质量, WebP
 */
export function getSmallUrl(url: string | undefined | null): string {
  if (!url) return ''
  const processParams = 'image/resize,m_lfit,w_400,h_400/quality,q_85/format,webp'
  return addOssProcessParams(url, processParams)
}

/**
 * 获取标准图 URL
 * 规格: 最大边 1200px, 85% 质量, WebP
 */
export function getNormalUrl(url: string | undefined | null): string {
  if (!url) return ''
  const processParams = 'image/resize,m_lfit,w_1200/quality,q_85/format,webp'
  return addOssProcessParams(url, processParams)
}

/**
 * 获取原图 URL
 * 移除 OSS 处理参数，保留其他参数（签名等）
 */
export function getOriginalUrl(url: string | undefined | null): string {
  if (!url) return ''

  try {
    const urlObj = new URL(url)
    urlObj.searchParams.delete('x-oss-process')
    return urlObj.toString()
  } catch (e) {
    // 回退方案
    console.warn('URL 解析失败，使用回退方案:', url)
    return url.replace(/[?&]x-oss-process=[^&]*/g, '')
  }
}
