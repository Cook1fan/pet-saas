import request from '@/utils/request'

export interface QrCodeVerifyReq {
  scene: string
}

export interface QrCodeVerifyResp {
  valid: boolean
  tenantId: number
  tenantName: string
  tenantLogo: string
  isBound: boolean
}

export interface QrCodeBindReq {
  tenantId: number
  memberId: number
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 验证二维码有效性
export function verifyQrCode(scene: string) {
  return request.get<ApiResponse<QrCodeVerifyResp>>('/api/user/qrcode/verify', {
    params: { scene }
  })
}

// 扫码绑定（注册/登录后调用）
export function bindTenant(data: QrCodeBindReq) {
  return request.post<ApiResponse<void>>('/api/user/qrcode/bind', data)
}

// 检查用户是否已绑定该店铺
export function checkUserBinding(tenantId: number) {
  return request.get<ApiResponse<{ bound: boolean }>>('/api/user/qrcode/check-binding', {
    params: { tenantId }
  })
}