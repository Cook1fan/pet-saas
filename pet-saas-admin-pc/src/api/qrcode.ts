import request from '@/utils/request'

// 二维码类型枚举
export const QR_TYPE = {
  SHOP: 1,
  GOODS: 2,
  ACTIVITY: 3
} as const

export type QrType = typeof QR_TYPE[keyof typeof QR_TYPE]

// 二维码信息
export interface QrCodeInfo {
  qrCodeId: number
  qrName: string
  qrUrl: string
  qrType: QrType
  scanCount: number
  createTime: string
}

// 二维码统计
export interface QrCodeStats {
  totalScan: number
  newUser: number
  oldUser: number
  guestUser: number
  dailyStats: DailyStat[]
}

// 每日统计
export interface DailyStat {
  date: string
  scanCount: number
  newUser: number
}

// 获取店铺二维码
export function getQrCode(tenantId?: number) {
  return request.get<QrCodeInfo>('/pc/shop/qrcode', {
    params: tenantId ? { tenantId } : undefined
  })
}

// 下载二维码
export function downloadQrCode(qrCodeId: number) {
  return request.get<Blob>(`/pc/shop/qrcode/download`, {
    params: { qrCodeId },
    responseType: 'blob'
  })
}

// 刷新二维码
export function refreshQrCode(qrCodeId: number) {
  return request.post<void>('/pc/shop/qrcode/refresh', null, {
    params: { qrCodeId }
  })
}

// 获取二维码统计
export interface QrCodeStatsQuery {
  tenantId?: number
  startDate?: string
  endDate?: string
}

export function getQrCodeStats(params?: QrCodeStatsQuery) {
  return request.get<QrCodeStats>('/pc/shop/qrcode/stat', { params })
}
