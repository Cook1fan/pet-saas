import request from '@/utils/request'

export interface CopywritingReq {
  scene: string
  keyword: string
  tone?: string
}

export interface CopywritingResp {
  content: string
}

export function generateCopywriting(data: CopywritingReq) {
  return request.post<CopywritingResp>('/ai/copywriting', data)
}

export interface QuotaInfo {
  remaining: number
  total: number
  used: number
  expireTime?: string
}

export function getQuotaInfo() {
  return request.get<QuotaInfo>('/ai/quota')
}

export interface Package {
  id: number
  name: string
  count: number
  price: number
  originalPrice?: number
}

export function getPackageList() {
  return request.get<Package[]>('/ai/package/list')
}

export function purchasePackage(id: number) {
  return request.post(`/ai/package/${id}/purchase`)
}
