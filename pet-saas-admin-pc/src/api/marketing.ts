import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

export interface GroupBuyActivity {
  id: number
  name: string
  goodsName: string
  goodsId: number
  originalPrice: number
  groupPrice: number
  groupSize: number
  stock: number
  soldCount: number
  startTime: string
  endTime: string
  status: number
  createTime: string
}

export interface GroupBuyQuery extends PageQuery {
  name?: string
  status?: number
}

export function getGroupBuyList(params: GroupBuyQuery) {
  return request.get<PageResult<GroupBuyActivity>>('/marketing/group-buy/list', { params })
}

export function getGroupBuy(id: number) {
  return request.get<GroupBuyActivity>(`/marketing/group-buy/${id}`)
}

export function createGroupBuy(data: Partial<GroupBuyActivity>) {
  return request.post('/marketing/group-buy', data)
}

export function updateGroupBuy(id: number, data: Partial<GroupBuyActivity>) {
  return request.put(`/marketing/group-buy/${id}`, data)
}

export function deleteGroupBuy(id: number) {
  return request.delete(`/marketing/group-buy/${id}`)
}

export interface FlashSaleActivity {
  id: number
  name: string
  goodsName: string
  goodsId: number
  originalPrice: number
  salePrice: number
  stock: number
  soldCount: number
  startTime: string
  endTime: string
  status: number
  createTime: string
}

export interface FlashSaleQuery extends PageQuery {
  name?: string
  status?: number
}

export function getFlashSaleList(params: FlashSaleQuery) {
  return request.get<PageResult<FlashSaleActivity>>('/marketing/flash-sale/list', { params })
}

export function getFlashSale(id: number) {
  return request.get<FlashSaleActivity>(`/marketing/flash-sale/${id}`)
}

export function createFlashSale(data: Partial<FlashSaleActivity>) {
  return request.post('/marketing/flash-sale', data)
}

export function updateFlashSale(id: number, data: Partial<FlashSaleActivity>) {
  return request.put(`/marketing/flash-sale/${id}`, data)
}

export function deleteFlashSale(id: number) {
  return request.delete(`/marketing/flash-sale/${id}`)
}

export interface MarketingData {
  totalActivityCount: number
  totalOrderCount: number
  totalGmv: number
  activityData: Array<{
    name: string
    orderCount: number
    gmv: number
  }>
}

export function getMarketingData() {
  return request.get<MarketingData>('/marketing/data')
}
