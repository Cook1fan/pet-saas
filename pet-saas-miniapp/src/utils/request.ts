/**
 * 网络请求封装
 */
interface RequestOptions {
  url: string
  method: string
  data?: any
  params?: any
  header?: any
}

interface ResponseData<T = any> {
  code: number
  data: T
  message: string
}

const BASE_URL = 'http://localhost:8080'

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

  private request<T = any>(options: RequestOptions): Promise<ResponseData<T>> {
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
            } else if (res.data.code === 401) {
              // 未登录，跳转登录页
              uni.showToast({
                title: '请先登录',
                icon: 'none'
              })
              uni.navigateTo({
                url: '/pages/user/login'
              })
              reject(res.data)
            } else {
              uni.showToast({
                title: res.data.message || '请求失败',
                icon: 'none'
              })
              reject(res.data)
            }
          } else {
            uni.showToast({
              title: '网络错误',
              icon: 'none'
            })
            reject(res)
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(err)
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
