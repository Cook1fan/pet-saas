import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

export interface ShopConfig {
  id: number
  name: string
  address: string
  phone: string
  logo?: string
}

export function getShopConfig() {
  return request.get<ShopConfig>('/shop/config')
}

export function updateShopConfig(data: Partial<ShopConfig>) {
  return request.put('/shop/config', data)
}

export interface Staff {
  id: number
  username: string
  name: string
  phone: string
  role: string
  status: number
}

export interface StaffQuery extends PageQuery {
  name?: string
  phone?: string
}

export function getStaffList(params: StaffQuery) {
  return request.get<PageResult<Staff>>('/shop/staff/list', { params })
}

export function createStaff(data: Partial<Staff>) {
  return request.post('/shop/staff', data)
}

export function updateStaff(id: number, data: Partial<Staff>) {
  return request.put(`/shop/staff/${id}`, data)
}

export function deleteStaff(id: number) {
  return request.delete(`/shop/staff/${id}`)
}

// 支付配置
export interface PaymentConfig {
  id: number
  mchId: string
  mchKey: string
  certPath?: string
}

export function getPaymentConfig() {
  return request.get<PaymentConfig>('/shop/payment/config')
}

export function updatePaymentConfig(data: Partial<PaymentConfig>) {
  return request.put('/shop/payment/config', data)
}
