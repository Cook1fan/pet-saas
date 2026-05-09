import request from '@/utils/request'

export interface ShopDashboardData {
  todayOrders: number
  todayGMV: number
  totalMembers: number
  lowStockGoods: number
  recentOrders: Array<{
    id: number
    orderNo: string
    memberName?: string
    amount: number
    status: string
    createTime: string
  }>
  orderTrend: Array<{ date: string; count: number; amount: number }>
}

export function getShopDashboard() {
  return request.get<ShopDashboardData>('/shop/dashboard')
}
