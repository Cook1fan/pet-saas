import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

// ==================== 枚举定义 ====================

// 支付方式枚举
export const PayTypeEnum = {
  WECHAT: 1,      // 微信支付
  CASH: 2,        // 现金
  BALANCE: 3,     // 储值余额
  CARD: 4         // 次卡
} as const

export type PayType = typeof PayTypeEnum[keyof typeof PayTypeEnum]

// 支付状态枚举
export const PayStatusEnum = {
  UNPAID: 0,       // 待支付
  PAID: 1,         // 已支付
  REFUNDED: 2      // 已退款
} as const

export type PayStatus = typeof PayStatusEnum[keyof typeof PayStatusEnum]

// 订单状态枚举
export const OrderStatusEnum = {
  UNPAID: 0,       // 待支付
  PAID: 1,         // 已支付
  SHIPPED: 2,      // 已发货
  COMPLETED: 3,    // 已完成
  CANCELLED: 4,    // 已取消
  REFUNDED: 5      // 已退款
} as const

export type OrderStatus = typeof OrderStatusEnum[keyof typeof OrderStatusEnum]

// ==================== 类型定义 ====================

// 订单明细项
export interface OrderItem {
  id: number
  goodsId: number
  goodsName: string
  num: number
  price: number
}

// 订单主信息
export interface Order {
  id: number
  orderNo: string
  memberId?: number
  totalAmount: number
  payAmount: number
  payType: PayType
  payStatus: PayStatus
  payTime?: string
  transactionId?: string
  createTime: string
}

// 订单列表项
export interface OrderListItem {
  id: number
  orderNo: string
  totalAmount: number
  payAmount: number
  payType: PayType
  payStatus: PayStatus
  orderStatus: OrderStatus
  payTime?: string
  transactionId?: string
  createTime: string
}

// 订单详情（含明细）
export interface OrderDetail {
  order: Order
  items: OrderItem[]
}

// 订单查询参数
export interface OrderListQuery extends PageQuery {
  memberId?: number
  payType?: PayType
  orderStatus?: OrderStatus
  orderNo?: string
  startTime?: string
  endTime?: string
}

// 开单收银请求
export interface CreateOrderRequest {
  memberId?: number
  items: Array<{
    skuId: number
    num: number
  }>
  payType: PayType
  cardId?: number        // 次卡支付时必填
}

// 开单收银响应
export interface CreateOrderResponse {
  id: number
  orderNo: string
  totalAmount: number
  payAmount: number
  payType: PayType
  payStatus: PayStatus
  wechatPayParams?: {    // 微信支付时返回
    codeUrl?: string     // PC 端扫码支付二维码链接
  }
}

// 退款请求
export interface RefundOrderRequest {
  orderId: number
  refundAmount: number
  reason: string
}

// ==================== 工具函数 ====================

// 获取支付方式文本
export function getPayTypeText(payType: PayType): string {
  const map: Record<PayType, string> = {
    [PayTypeEnum.WECHAT]: '微信支付',
    [PayTypeEnum.CASH]: '现金',
    [PayTypeEnum.BALANCE]: '储值余额',
    [PayTypeEnum.CARD]: '次卡'
  }
  return map[payType] || '未知'
}

// 获取支付状态文本
export function getPayStatusText(payStatus: PayStatus): string {
  const map: Record<PayStatus, string> = {
    [PayStatusEnum.UNPAID]: '待支付',
    [PayStatusEnum.PAID]: '已支付',
    [PayStatusEnum.REFUNDED]: '已退款'
  }
  return map[payStatus] || '未知'
}

// 获取支付状态 Tag 类型
export function getPayStatusTagType(payStatus: PayStatus): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<PayStatus, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    [PayStatusEnum.UNPAID]: 'warning',
    [PayStatusEnum.PAID]: 'success',
    [PayStatusEnum.REFUNDED]: 'danger'
  }
  return map[payStatus] || 'info'
}

// 获取订单状态文本
export function getOrderStatusText(orderStatus: OrderStatus): string {
  const map: Record<OrderStatus, string> = {
    [OrderStatusEnum.UNPAID]: '待支付',
    [OrderStatusEnum.PAID]: '已支付',
    [OrderStatusEnum.SHIPPED]: '已发货',
    [OrderStatusEnum.COMPLETED]: '已完成',
    [OrderStatusEnum.CANCELLED]: '已取消',
    [OrderStatusEnum.REFUNDED]: '已退款'
  }
  return map[orderStatus] || '未知'
}

// 获取订单状态 Tag 类型
export function getOrderStatusTagType(orderStatus: OrderStatus): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<OrderStatus, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    [OrderStatusEnum.UNPAID]: 'warning',
    [OrderStatusEnum.PAID]: 'primary',
    [OrderStatusEnum.SHIPPED]: 'info',
    [OrderStatusEnum.COMPLETED]: 'success',
    [OrderStatusEnum.CANCELLED]: 'info',
    [OrderStatusEnum.REFUNDED]: 'danger'
  }
  return map[orderStatus] || 'info'
}

// ==================== 接口定义 ====================

// 开单收银
export function createOrder(data: CreateOrderRequest) {
  return request.post<CreateOrderResponse>('/pc/order/create', data)
}

// 订单列表
export function getOrderList(params: OrderListQuery) {
  return request.get<PageResult<OrderListItem>>('/pc/order/list', { params })
}

// 订单详情
export function getOrderDetail(orderId: number) {
  return request.get<OrderDetail>(`/pc/order/detail/${orderId}`)
}

// 订单退款
export function refundOrder(data: RefundOrderRequest) {
  return request.post('/pc/order/refund', data)
}
