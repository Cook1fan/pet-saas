import request from '@/utils/request'

export interface ShopVO {
  tenantId: number
  shopName: string
  address: string
  phone: string
  isCurrent: boolean
  bindTime?: string
}

export interface ShopSwitchReq {
  tenantId: number
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 获取已绑定门店列表
export function getBoundShopList() {
  return request.get<ApiResponse<ShopVO[]>>('/api/user/shop/list')
}

// 获取可选门店列表（用于首次选择门店）
export function getAvailableShopList() {
  return request.get<ApiResponse<ShopVO[]>>('/api/user/shop/available')
}

// 切换门店
export function switchShop(data: ShopSwitchReq) {
  return request.post<ApiResponse<void>>('/api/user/shop/switch', data)
}

// 绑定门店（首次选择或切换）
export function bindShop(data: ShopSwitchReq) {
  return request.post<ApiResponse<void>>('/api/user/shop/bind', data)
}
