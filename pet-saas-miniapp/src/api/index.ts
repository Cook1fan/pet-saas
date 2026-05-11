import request from '@/utils/request'
import type {
  ApiResponse,
  PageResponse,
  ShopInfo,
  MemberInfo,
  PetInfo,
  BannerItem,
  QuickNavItem,
  ActivityItem,
  ActivityDetail,
  OrderInfo,
  MemberCard,
  ServiceReminder,
  ChatMessage,
  MemberStatistics
} from '@/types'

// ==================== 首页相关 ====================
export interface HomeData {
  shopInfo: ShopInfo
  bannerList: BannerItem[]
  quickNavList: QuickNavItem[]
  flashSaleList: ActivityItem[]
  groupBuyList: ActivityItem[]
  allActivityList?: ActivityItem[]
  hasNewNotice: boolean
}

export function getHomeData() {
  return request.get<ApiResponse<HomeData>>('/api/miniapp/home/index')
}

// ==================== 门店相关 ====================
export function getNearbyShops(params?: { latitude?: number; longitude?: number; keyword?: string }) {
  return request.get<ApiResponse<ShopInfo[]>>('/api/miniapp/shop/nearby', { params })
}

export function getMyShops() {
  return request.get<ApiResponse<ShopInfo[]>>('/api/miniapp/shop/my-list')
}

export function bindShop(data: { tenantId: string }) {
  return request.post<ApiResponse<void>>('/api/miniapp/shop/bind', data)
}

export function switchShop(data: { tenantId: string }) {
  return request.post<ApiResponse<void>>('/api/miniapp/shop/switch', data)
}

// ==================== 活动相关 ====================
export interface ActivityListParams {
  type?: 'all' | 'group' | 'flash'
  status?: 'all' | 'ongoing' | 'upcoming'
  page?: number
  pageSize?: number
}

export function getActivityList(params: ActivityListParams) {
  return request.get<ApiResponse<PageResponse<ActivityItem>>>('/api/miniapp/activity/list', { params })
}

export function getActivityDetail(id: number) {
  return request.get<ApiResponse<ActivityDetail>>(`/api/miniapp/activity/detail/${id}`)
}

// ==================== 订单相关 ====================
export interface CreateOrderParams {
  activityId: number
  groupId?: number
  skuId: number
  petId?: number
  remark?: string
  payType: 'wechat' | 'balance'
}

export function createOrder(data: CreateOrderParams) {
  return request.post<ApiResponse<OrderInfo>>('/api/miniapp/order/create', data)
}

export function getOrderDetail(orderId: string) {
  return request.get<ApiResponse<OrderInfo>>(`/api/miniapp/order/detail/${orderId}`)
}

export function getOrderList(params?: { status?: string; page?: number; pageSize?: number }) {
  return request.get<ApiResponse<PageResponse<OrderInfo>>>('/api/miniapp/order/list', { params })
}

export function cancelOrder(orderId: string) {
  return request.post<ApiResponse<void>>('/api/miniapp/order/cancel', { orderId })
}

// ==================== 会员相关 ====================
export interface MemberIndexData {
  memberInfo: MemberInfo
  currentShop: ShopInfo
  statistics: MemberStatistics
  reminders: ServiceReminder[]
}

export function getMemberIndex() {
  return request.get<ApiResponse<MemberIndexData>>('/api/miniapp/member/index')
}

export function getMemberPets() {
  return request.get<ApiResponse<PetInfo[]>>('/api/miniapp/member/pets')
}

// ==================== 次卡相关 ====================
export interface CardRule {
  id: number
  name: string
  totalTimes: number
  price: number
  validDays?: number
  tag?: string
}

export function getCardRuleList() {
  return request.get<ApiResponse<CardRule[]>>('/api/miniapp/card/rule-list')
}

export function purchaseCard(data: { cardRuleId: number }) {
  return request.post<ApiResponse<{ orderId: number; orderNo: string; payAmount: number; wechatPayParams?: any }>>('/api/miniapp/card/purchase', data)
}

export function getCardList() {
  return request.get<ApiResponse<MemberCard[]>>('/api/miniapp/card/list')
}

export function getCardDetail(id: number) {
  return request.get<ApiResponse<MemberCard>>(`/api/miniapp/card/detail/${id}`)
}

export function generateVerifyCode(cardId: number) {
  return request.post<ApiResponse<{ verifyCode: string; expireTime: string; remainSeconds: number }>>('/api/miniapp/card/generate-code', { cardId })
}

export interface CardRecord {
  id: number
  type: 'purchase' | 'verify'
  cardName: string
  times: number
  remainTimes: number
  createTime: string
}

export function getCardRecordList(params?: { page?: number; pageSize?: number; type?: string }) {
  return request.get<ApiResponse<PageResponse<CardRecord>>>('/api/miniapp/card/records', { params })
}

// ==================== 储值相关 ====================
export interface RechargeRule {
  id: number
  name: string
  rechargeAmount: number
  bonusAmount: number
  tag?: string
}

export interface UserRechargeAccount {
  balance: number
  totalRecharge: number
  totalConsume: number
}

export interface RechargeRecord {
  id: number
  type: 'recharge' | 'consume' | 'refund'
  amount: number
  balanceBefore: number
  balanceAfter: number
  remark?: string
  createTime: string
}

export function getRechargeRuleList() {
  return request.get<ApiResponse<RechargeRule[]>>('/api/miniapp/recharge/rule-list')
}

export function purchaseRecharge(data: { rechargeRuleId: number }) {
  return request.post<ApiResponse<{ orderId: number; orderNo: string; payAmount: number; wechatPayParams?: any }>>('/api/miniapp/recharge/purchase', data)
}

export function getMyRechargeAccount() {
  return request.get<ApiResponse<UserRechargeAccount>>('/api/miniapp/recharge/account')
}

export function getRechargeRecordList(params?: { page?: number; pageSize?: number; type?: string }) {
  return request.get<ApiResponse<PageResponse<RechargeRecord>>>('/api/miniapp/recharge/records', { params })
}

// ==================== AI 客服 ====================
export function sendChatMessage(message: string) {
  return request.post<ApiResponse<{ reply: string; isTransferManual: boolean }>>('/api/miniapp/ai/chat', { message })
}

export function getChatHistory() {
  return request.get<ApiResponse<ChatMessage[]>>('/api/miniapp/ai/history')
}

// ==================== 海报生成 ====================
export function generateActivityPoster(activityId: number) {
  return request.post<ApiResponse<{ posterUrl: string }>>('/api/miniapp/activity/generate-poster', { activityId })
}
