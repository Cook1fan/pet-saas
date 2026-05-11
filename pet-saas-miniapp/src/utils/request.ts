/**
 * 网络请求封装
 */
import type { ApiResponse } from '@/types'

interface RequestOptions {
  url: string
  method: string
  data?: any
  params?: any
  header?: any
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 获取本地存储的token
function getToken(): string {
  return uni.getStorageSync('token') || ''
}

class Request {
  private get<T = any>(url: string, options?: Omit<RequestOptions, 'url' | 'method'>) {
    return this.request<T>({
      url,
      method: 'GET',
      ...options
    })
  }

  private post<T = any>(url: string, data?: any, options?: Omit<RequestOptions, 'url' | 'method' | 'data'>) {
    return this.request<T>({
      url,
      method: 'POST',
      data,
      ...options
    })
  }

  private handleError(error: any): never {
    if (error.code === 401) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      uni.reLaunch({ url: '/pages/welcome/index' })
    } else {
      uni.showToast({
        title: error.message || '请求失败',
        icon: 'none'
      })
    }
    throw error
  }

  private request<T = any>(options: RequestOptions): Promise<ApiResponse<T>> {
    const token = getToken()
    return new Promise((resolve, reject) => {
      uni.request({
        url: BASE_URL + options.url,
        method: options.method as any,
        data: options.data || options.params,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : '',
          ...options.header
        },
        success: (res: any) => {
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data)
            } else {
              reject(this.handleError(res.data))
            }
          } else {
            const error = {
              code: res.statusCode,
              message: '网络错误'
            }
            reject(this.handleError(error))
          }
        },
        fail: (err) => {
          const error = {
            code: -1,
            message: '网络错误'
          }
          reject(this.handleError(error))
        }
      })
    })
  }
}

// 创建 request 实例
const request = new Request()

export default {
  get: request.get.bind(request),
  post: request.post.bind(request)
}
