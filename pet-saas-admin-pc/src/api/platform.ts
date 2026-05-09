import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

export interface Tenant {
  tenantId: number
  shopName: string
  adminPhone: string
  address?: string
  status: number
  createTime: string
}

export interface TenantQuery extends PageQuery {
  shopName?: string
  adminPhone?: string
  status?: number
}

export function getTenantList(params: TenantQuery) {
  return request.get<PageResult<Tenant>>('/platform/tenant/list', { params })
}

export interface CreateTenantReq {
  shopName: string
  adminPhone: string
  address: string
}

export function createTenant(data: CreateTenantReq) {
  return request.post('/platform/tenant/create', data)
}

export interface UpdateTenantStatusReq {
  tenantId: number
  status?: number
  resetPassword?: boolean
}

export function toggleTenantStatus(tenantId: number, status: number) {
  return request.post('/platform/tenant/updateStatus', { tenantId, status })
}

export function resetTenantPassword(tenantId: number) {
  return request.post('/platform/tenant/updateStatus', { tenantId, resetPassword: true })
}

export interface PlatformDashboardData {
  totalTenants: number
  totalMembers: number
  todayOrders: number
  todayGMV: number
  orderTrend: Array<{ date: string; count: number; amount: number }>
}

export function getPlatformDashboard() {
  return request.get<PlatformDashboardData>('/platform/dashboard')
}
