import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

// 储值规则
export interface RechargeRule {
  id: number
  name: string
  rechargeAmount: number
  bonusAmount: number
  status: number
  createTime: string
}

export interface RechargeRuleQuery extends PageQuery {
  name?: string
  status?: number
}

export function getRechargeRuleList(params: RechargeRuleQuery) {
  return request.get<PageResult<RechargeRule>>('/recharge/rule/list', { params })
}

export function getRechargeRule(id: number) {
  return request.get<RechargeRule>(`/recharge/rule/${id}`)
}

export function createRechargeRule(data: Partial<RechargeRule>) {
  return request.post('/recharge/rule', data)
}

export function updateRechargeRule(id: number, data: Partial<RechargeRule>) {
  return request.put(`/recharge/rule/${id}`, data)
}

export function deleteRechargeRule(id: number) {
  return request.delete(`/recharge/rule/${id}`)
}

// 次卡规则
export interface CardRule {
  id: number
  name: string
  totalTimes: number
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
  return request.get<PageResult<CardRule>>('/card/rule/list', { params })
}

export function getCardRule(id: number) {
  return request.get<CardRule>(`/card/rule/${id}`)
}

export function createCardRule(data: Partial<CardRule>) {
  return request.post('/card/rule', data)
}

export function updateCardRule(id: number, data: Partial<CardRule>) {
  return request.put(`/card/rule/${id}`, data)
}

export function deleteCardRule(id: number) {
  return request.delete(`/card/rule/${id}`)
}

// 核销记录
export interface RechargeRecord {
  id: number
  type: 'recharge' | 'card'
  memberName: string
  memberPhone: string
  ruleName: string
  amount?: number
  times?: number
  operator: string
  createTime: string
}

export interface RechargeRecordQuery extends PageQuery {
  type?: string
  memberName?: string
  startTime?: string
  endTime?: string
}

export function getRechargeRecordList(params: RechargeRecordQuery) {
  return request.get<PageResult<RechargeRecord>>('/recharge/record/list', { params })
}
