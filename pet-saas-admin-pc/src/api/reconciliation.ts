import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

export interface FlowRecord {
  id: number
  type: 'income' | 'expense'
  amount: number
  balance: number
  payType?: string
  orderNo?: string
  remark?: string
  createTime: string
}

export interface FlowQuery extends PageQuery {
  type?: string
  payType?: string
  startTime?: string
  endTime?: string
}

export function getFlowList(params: FlowQuery) {
  return request.get<PageResult<FlowRecord>>('/reconciliation/flow/list', { params })
}

export interface ReconciliationItem {
  id: number
  date: string
  systemAmount: number
  wechatAmount: number
  diffAmount: number
  status: 'normal' | 'abnormal'
  createTime: string
}

export interface ReconciliationQuery extends PageQuery {
  date?: string
  status?: string
}

export function getReconciliationList(params: ReconciliationQuery) {
  return request.get<PageResult<ReconciliationItem>>('/reconciliation/detail/list', { params })
}
