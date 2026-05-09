import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import router from '@/router'
import type { ApiResponse } from './types'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

// 需要静默失败的 URL 关键词
const SILENT_URLS = ['dashboard', '/auth/current', '/auth/logout', '/platform/logout', '/pc/logout', '/shop/config', '/pc/goods', '/shop/dashboard']

function isSilentUrl(url: string | undefined): boolean {
  if (!url) return false
  return SILENT_URLS.some(keyword => url.includes(keyword))
}

const service: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    const appStore = useAppStore()

    // 开始请求，显示进度条
    appStore.startRequest()

    if (userStore.token) {
      // 尝试多种可能的 token 字段名
      config.headers.satoken = userStore.token
      config.headers.Authorization = `Bearer ${userStore.token}`
      config.headers.token = userStore.token
      console.log('请求添加 token:', config.url, userStore.token.substring(0, 20) + '...')
    }
    return config
  },
  (error) => {
    const appStore = useAppStore()
    // 请求出错时也要结束进度条
    appStore.finishRequest()
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const appStore = useAppStore()
    const userStore = useUserStore()
    const res = response.data
    const url = response.config?.url || ''
    console.log('接口响应:', url, res)

    // 响应成功，结束进度条
    appStore.finishRequest()

    // 处理多种可能的响应格式
    if (res !== undefined && res !== null) {
      // 格式1: { code: 200, data: ... } 或 { code: 0, data: ... }
      if (typeof res.code === 'number' && (res.code === 200 || res.code === 0)) {
        return res.data !== undefined ? res.data : res
      }
      // 格式2: 直接就是数据（没有 code 包装）
      if (res.code === undefined) {
        return res
      }
      // 处理 401 token 无效
      if (res.code === 401) {
        console.log('Token 无效，清除登录状态并跳转到登录页')
        userStore.logout()
        // 根据当前路径判断跳转到哪个登录页
        const currentPath = window.location.pathname
        const isPlatform = currentPath.startsWith('/platform')
        router.push(isPlatform ? '/platform/login' : '/shop/login')
        return Promise.reject(new Error(res.message || 'Token 无效，请重新登录'))
      }
      // 有 code 但不是成功码
      if (!isSilentUrl(url)) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const appStore = useAppStore()
    const url = error.config?.url || ''
    console.error('请求错误:', url, error)

    // 响应失败，结束进度条
    appStore.finishRequest()

    if (!isSilentUrl(url)) {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}
