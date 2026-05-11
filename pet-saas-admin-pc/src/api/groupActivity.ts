import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

// 活动类型枚举
export const ACTIVITY_TYPE = {
  GROUP: 1,
  SECKILL: 2
} as const

// 活动状态枚举
export const ACTIVITY_STATUS = {
  OFFLINE: 0,
  ONLINE: 1
} as const

export type ActivityType = typeof ACTIVITY_TYPE[keyof typeof ACTIVITY_TYPE]
export type ActivityStatus = typeof ACTIVITY_STATUS[keyof typeof ACTIVITY_STATUS]

// 活动信息类型
export interface ActivityInfoVO {
  id: number
  tenantId: number
  type: ActivityType
  title: string
  goodsId: number
  goodsName: string
  goodsImage?: string
  skuId: number
  skuSpec?: string
  price: number
  originPrice: number
  groupCount?: number
  groupValidHours?: number
  stock: number
  soldCount: number
  limitNum: number
  startTime: string
  endTime: string
  status: ActivityStatus
  createTime: string
  updateTime: string
}

// 活动查询参数
export interface ActivityQuery extends PageQuery {
  title?: string
  type?: ActivityType
  status?: ActivityStatus
}

// 创建拼团活动请求参数
export interface CreateGroupActivityReq {
  title: string
  goodsId: number
  skuId: number
  price: number
  originPrice: number
  groupCount: number
  groupValidHours: number
  stock: number
  limitNum: number
  startTime: string
  endTime: string
}

// 创建秒杀活动请求参数
export interface CreateSeckillActivityReq {
  title: string
  goodsId: number
  price: number
  originPrice: number
  stock: number
  limitNum: number
  startTime: string
  endTime: string
}

// 活动数据统计
export interface ActivityData {
  totalOrderCount: number
  successGroupCount?: number
  failedGroupCount?: number
  ongoingGroupCount?: number
  totalGmv: number
}

// 拼团活动管理API
export function getActivityList(params: ActivityQuery) {
  return request.get<PageResult<ActivityInfoVO>>('/pc/activity/list', { params })
}

export function createGroupActivity(data: CreateGroupActivityReq) {
  return request.post<ActivityInfoVO>('/pc/activity/group/create', data)
}

export function updateGroupActivity(id: number, data: CreateGroupActivityReq) {
  return request.put<ActivityInfoVO>(`/pc/activity/group/update/${id}`, data)
}

export function createSeckillActivity(data: CreateSeckillActivityReq) {
  return request.post<ActivityInfoVO>('/pc/activity/seckill/create', data)
}

export function updateSeckillActivity(id: number, data: CreateSeckillActivityReq) {
  return request.put<ActivityInfoVO>(`/pc/activity/seckill/update/${id}`, data)
}

export function getActivityDetail(id: number) {
  return request.get<ActivityInfoVO>(`/pc/activity/${id}`)
}

export function deleteActivity(id: number) {
  return request.delete(`/pc/activity/${id}`)
}

export function updateActivityStatus(id: number, status: ActivityStatus) {
  return request.put(`/pc/activity/${id}/status`, null, { params: { status } })
}

export function getActivityData(id: number) {
  return request.get<ActivityData>(`/pc/activity/data/${id}`)
}

// 拼团组管理API
export interface GroupBuyGroup {
  id: number
  groupNo: string
  leaderMemberName: string
  leaderMemberPhone: string
  currentNum: number
  targetNum: number
  status: number
  expireTime: string
  successTime?: string
  createTime: string
  members?: GroupBuyGroupMember[]
}

export interface GroupBuyGroupMember {
  memberName: string
  memberPhone: string
  isLeader: boolean
  joinTime: string
}

export interface GroupBuyGroupQuery extends PageQuery {
  status?: number
}

export function getActivityGroupList(activityId: number, params: GroupBuyGroupQuery) {
  return request.get<PageResult<GroupBuyGroup>>(`/pc/activity/group/detail/${activityId}/groups`, { params })
}
