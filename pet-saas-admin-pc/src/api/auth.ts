import request from '@/utils/request'

export interface ShopLoginReq {
  username: string
  password: string
}

export interface PlatformLoginReq {
  username: string
  password: string
}

// 平台管理员信息
export interface PlatformAdmin {
  id: number
  username: string
  role: string
  createTime?: string
}

// 门店管理员信息
export interface ShopAdmin {
  id: number
  tenantId?: number
  username: string
  role: string
  status?: number
  createTime?: string
}

export interface LoginResp {
  token: string
  user: PlatformAdmin | ShopAdmin
}

export interface UserInfo {
  id: number
  username: string
  role: string
  tenantId?: number
}

export function shopLogin(data: ShopLoginReq) {
  return request.post<LoginResp>('/pc/login', data)
}

export function platformLogin(data: PlatformLoginReq) {
  return request.post<LoginResp>('/platform/login', data)
}

export function getCurrentUser() {
  return request.get<UserInfo>('/auth/current')
}

export function platformLogout() {
  return request.post('/platform/logout')
}

export function shopLogout() {
  return request.post('/pc/logout')
}

/**
 * @deprecated 请使用 platformLogout 或 shopLogout 替代
 */
export function logout() {
  return request.post('/auth/logout')
}

export interface TenantOption {
  id: number
  name: string
}

export function getTenantList() {
  return request.get<TenantOption[]>('/tenant/options')
}

// 临时模拟数据
export function getTenantListMock() {
  return Promise.resolve<TenantOption[]>([
    { id: 1, name: '宠爱有家宠物馆' },
    { id: 2, name: '萌宠乐园' }
  ])
}
