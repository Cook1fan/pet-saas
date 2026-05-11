import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

// 储值规则
export interface RechargeRule {
  id: number
  name: string
  rechargeAmount: number
  giveAmount: number
  status: number
  createTime: string
}

export interface RechargeRuleQuery extends PageQuery {
  keyword?: string
  status?: number
}

export function getRechargeRuleList(params: RechargeRuleQuery) {
  // 转换字段名以匹配后端接口
  const requestParams = {
    pageNum: params.page,
    pageSize: params.pageSize,
    keyword: params.keyword,
    status: params.status
  }
  return request.get<PageResult<RechargeRule>>('/pc/recharge/rule/list', { params: requestParams })
}

export function getRechargeRuleDetail(id: number) {
  return request.get<RechargeRule>(`/pc/recharge/rule/detail/${id}`)
}

export function createRechargeRule(data: Partial<RechargeRule>) {
  return request.post<{ id: number }>('/pc/recharge/rule/create', data)
}

export function updateRechargeRule(id: number, data: Partial<RechargeRule>) {
  return request.put(`/pc/recharge/rule/update/${id}`, data)
}

export function updateRechargeRuleStatus(id: number, status: number) {
  return request.put(`/pc/recharge/rule/status/${id}?status=${status}`)
}

export function deleteRechargeRule(id: number) {
  return request.delete(`/pc/recharge/rule/delete/${id}`)
}

// 次卡规则
export interface CardRule {
  id: number
  name: string
  times: number
  price: number
  validDays?: number
  status: number
  createTime: string
}

export interface CardRuleQuery extends PageQuery {
  name?: string
  status?: number
}

export function getCardRuleList(params: CardRuleQuery) {
  return request.get<PageResult<CardRule>>('/pc/card/rule/list', { params })
}

export function getCardRuleDetail(id: number) {
  return request.get<CardRule>(`/pc/card/rule/detail/${id}`)
}

export function createCardRule(data: Partial<CardRule>) {
  return request.post<{ id: number }>('/pc/card/rule/create', data)
}

export function updateCardRule(id: number, data: Partial<CardRule>) {
  return request.put(`/pc/card/rule/update/${id}`, data)
}

export function updateCardRuleStatus(id: number, status: number) {
  return request.put(`/pc/card/rule/status/${id}?status=${status}`)
}

export function deleteCardRule(id: number) {
  return request.delete(`/pc/card/rule/delete/${id}`)
}

// 会员储值账户
export interface MemberAccount {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  balance: number
  totalRecharge: number
  totalConsume?: number
  createTime: string
}

export interface MemberAccountQuery extends PageQuery {
  memberName?: string
  memberPhone?: string
}

export function getMemberAccountList(params: MemberAccountQuery) {
  return request.get<PageResult<MemberAccount>>('/pc/recharge/account/list', { params })
}

export function getMemberAccountDetail(memberId: number) {
  return request.get<MemberAccount>(`/pc/recharge/account/detail/${memberId}`)
}

// 会员次卡
export interface MemberCard {
  id: number
  cardName: string
  remainTimes: number
  totalTimes: number
  expireTime: string
  status: number
  statusDesc: string
}

export function getMemberCardList(memberId: number) {
  return request.get<MemberCard[]>(`/pc/card/member/${memberId}/cards`)
}

// 储值消费记录
export interface RechargeRecord {
  id: number
  type: 'recharge' | 'consume' | 'refund'
  memberName: string
  memberPhone: string
  amount: number
  balanceBefore: number
  balanceAfter: number
  remark?: string
  createTime: string
}

export interface RechargeRecordQuery extends PageQuery {
  memberId?: number
  memberName?: string
  type?: string
  startTime?: string
  endTime?: string
}

export function getRechargeRecordList(params: RechargeRecordQuery) {
  return request.get<PageResult<RechargeRecord>>('/pc/recharge/record/list', { params })
}

export function exportRechargeRecord(params: RechargeRecordQuery) {
  return request.get('/pc/recharge/record/export', { params, responseType: 'blob' })
}

// 次卡记录
export interface CardRecord {
  id: number
  type: 'purchase' | 'verify'
  memberName: string
  cardName: string
  times: number
  remainTimes: number
  operator?: string
  createTime: string
}

export interface CardRecordQuery extends PageQuery {
  memberName?: string
  type?: string
  startTime?: string
  endTime?: string
}

export function getCardRecordList(params: CardRecordQuery) {
  return request.get<PageResult<CardRecord>>('/pc/card/record/list', { params })
}

export function exportCardRecord(params: CardRecordQuery) {
  return request.get('/pc/card/record/export', { params, responseType: 'blob' })
}
