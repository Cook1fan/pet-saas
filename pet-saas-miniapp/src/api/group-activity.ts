import request from '@/utils/request'

// 后端返回的数据结构（与Java DTO匹配）
export interface UserGroupActivityListResp {
  id: number
  title: string
  goodsImage: string
  goodsName: string
  price: number
  originPrice: number
  groupCount: number
  remainStock: number
  endTime: number
  hotGroups?: HotGroup[]
}

export interface HotGroup {
  groupId: number
  currentNum: number
  targetNum: number
  leaderAvatar: string
  leaderName: string
  remainTime: number
}

export interface UserGroupActivityDetailResp {
  id: number
  title: string
  goodsId: number
  goodsName: string
  goodsImage: string
  goodsImages: string[]
  price: number
  originPrice: number
  groupCount: number
  remainStock: number
  limitNum: number
  endTime: number
  userLimitRemain: number
  ongoingGroups: OngoingGroup[]
}

export interface OngoingGroup {
  groupId: number
  groupNo: string
  currentNum: number
  targetNum: number
  leaderAvatar: string
  leaderName: string
  remainTime: number
  remainTimeDesc: string
}

export interface UserGroupDetailResp {
  groupId: number
  groupNo: string
  activityId: number
  title: string
  goodsImage: string
  goodsName: string
  price: number
  originPrice: number
  currentNum: number
  targetNum: number
  status: number
  expireTime: string
  remainTime: number
  remainTimeDesc: string
  canJoin: boolean
  members: GroupMember[]
  membersMissing: any[]
}

export interface GroupMember {
  memberId: number
  memberName: string
  memberAvatar?: string
  isLeader: boolean
  joinTime: string
}

export interface CreateGroupOrderReq {
  activityId: number
  groupId?: number
  num: number
}

export interface CreateGroupOrderResp {
  orderId: number
  orderNo: string
  payAmount: number
  wechatPayParams: WechatPayParams
}

export interface WechatPayParams {
  timeStamp: string
  nonceStr: string
  package: string
  signType: string
  paySign: string
}

export interface MyGroupOrderResp {
  id: number
  orderNo: string
  activityId: number
  activityTitle: string
  goodsImage: string
  goodsName: string
  price: number
  num: number
  payAmount: number
  groupId?: number
  groupStatus: number
  groupStatusDesc: string
  isLeader: boolean
  currentNum: number
  targetNum: number
  remainTime?: number
  createTime: string
  shareUrl: string
}

// API Response wrapper
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 获取拼团活动列表
export function getGroupActivityList() {
  return request.get<ApiResponse<UserGroupActivityListResp[]>>('/api/user/activity/group/list')
}

// 获取拼团活动详情
export function getGroupActivityDetail(activityId: number) {
  return request.get<ApiResponse<UserGroupActivityDetailResp>>(`/api/user/activity/group/detail/${activityId}`)
}

// 获取拼团组详情
export function getGroupDetail(groupId: number) {
  return request.get<ApiResponse<UserGroupDetailResp>>(`/api/user/activity/group/group-detail/${groupId}`)
}

// 发起拼团
export function launchGroup(data: CreateGroupOrderReq) {
  return request.post<ApiResponse<CreateGroupOrderResp>>('/api/user/activity/group/launch', data)
}

// 加入拼团
export function joinGroup(data: CreateGroupOrderReq) {
  return request.post<ApiResponse<CreateGroupOrderResp>>('/api/user/activity/group/join', data)
}

// 获取我的拼团订单列表
export function getMyGroupOrders(params: {
  pageNum: number
  pageSize: number
  status?: number
}) {
  return request.get<ApiResponse<{
    records: MyGroupOrderResp[]
    total: number
    current: number
    size: number
  }>>('/api/user/activity/group/my-orders', { params })
}
