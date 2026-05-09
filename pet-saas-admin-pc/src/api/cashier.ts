import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'
import type { Goods, GoodsSku, Category } from './inventory'

// ==================== 商品相关接口 ====================

// 商品列表查询参数
export interface GoodsListQuery extends PageQuery {
  categoryId?: number
  keyword?: string
  status?: number
}

// 商品列表（开单用）
export function getGoodsList(params: GoodsListQuery) {
  return request.get<PageResult<Goods>>('/pc/goods/list', { params })
}

// 扫码查询商品 SKU
export function scanGoodsSku(code: string) {
  return request.get<GoodsSku>(`/pc/goods/sku/scan/${code}`)
}

// ==================== 会员相关接口 ====================

export interface CashierMember {
  id: number
  phone: string
  name: string
  tags?: string
  openid?: string
  createTime: string
}

export interface MemberQuery extends PageQuery {
  keyword?: string
  tag?: string
}

// 会员列表
export function getMemberList(params: MemberQuery) {
  return request.get<PageResult<CashierMember>>('/pc/member/list', { params })
}

// 搜索会员（简化版，用于开单收银快速搜索）
export function searchMember(keyword: string) {
  return request.get<CashierMember[]>('/cashier/member/search', { params: { keyword } })
}

// 会员储值账户
export interface MemberAccount {
  id: number
  memberId: number
  balance: number
  totalRecharge: number
}

export function getMemberAccount(memberId: number) {
  return request.get<MemberAccount>(`/pc/member/account/${memberId}`)
}

// ==================== 会员次卡相关接口 ====================

export interface MemberCard {
  id: number
  cardRuleId: number
  cardName: string
  remainTimes: number
  expireTime: string
  status: number
}

// 获取会员次卡列表
export function getMemberCards(memberId: number) {
  return request.get<MemberCard[]>(`/pc/member/${memberId}/cards`)
}

// ==================== 兼容旧接口（保留）====================

export interface CashierGoodsQuery extends PageQuery {
  name?: string
  categoryId?: number
  type?: string
}

export function getCashierGoodsList(params: CashierGoodsQuery) {
  return request.get<PageResult<Goods>>('/cashier/goods/list', { params })
}

export interface CashierOrderItem {
  goodsId: number
  goodsName: string
  goodsType: string
  price: number
  quantity: number
}

export interface CashierSubmitReq {
  memberId?: number
  items: CashierOrderItem[]
  payType: string
  payAmount: number
  remark?: string
}

export interface CashierSubmitResp {
  orderId: number
  orderNo: string
  payUrl?: string
}

export function submitCashier(data: CashierSubmitReq) {
  return request.post<CashierSubmitResp>('/cashier/submit', data)
}
