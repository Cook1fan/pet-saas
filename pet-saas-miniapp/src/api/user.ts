import request from '@/utils/request'

export interface WxLoginReq {
  code: string
  tenantId?: number
}

export interface MemberVO {
  id: number
  nickname?: string
  avatar?: string
  phone?: string
}

export interface WxLoginResp {
  token: string
  member: MemberVO
  isNewMember: boolean
  openid: string
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 微信授权登录
export function wxLogin(data: WxLoginReq) {
  return request.post<ApiResponse<WxLoginResp>>('/api/user/wxLogin', data)
}

// 获取会员信息
export function getMemberInfo() {
  return request.get<ApiResponse<MemberVO>>('/api/user/member/info')
}

// C端登出
export function logout() {
  return request.post<ApiResponse<void>>('/api/user/logout')
}
