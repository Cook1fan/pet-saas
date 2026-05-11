// 通用 API 响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// 门店信息
export interface ShopInfo {
  tenantId: string
  shopName: string
  shopLogo: string
  address?: string
  distance?: number
  businessHours?: string
  isCurrent?: boolean
}

// 会员信息
export interface MemberInfo {
  id: number
  nickname?: string
  avatar?: string
  phone?: string
}

// 宠物信息
export interface PetInfo {
  id: number
  name: string
  avatar?: string
  breed: string
  gender: 'male' | 'female'
}

// Banner
export interface BannerItem {
  id: number
  imageUrl: string
  linkType: 'activity' | 'page' | 'none'
  linkValue: string
}

// 快捷入口
export interface QuickNavItem {
  id: number
  icon: string
  name: string
  path: string
}

// 活动类型
export type ActivityType = 'group' | 'flash'
export type ActivityStatus = 'pending' | 'ongoing' | 'ended'

// 活动信息
export interface ActivityItem {
  id: number
  type: ActivityType
  title: string
  coverImage: string
  originalPrice: number
  activityPrice: number
  stock: number
  soldCount: number
  status: ActivityStatus
  startTime: string
  endTime: string
  groupInfo?: {
    currentCount: number
    targetCount: number
    deadline: string
  }
  flashInfo?: {
    endTime: string
    remainingSeconds: number
    soldPercent: number
  }
}

// 活动详情 - SKU
export interface ActivitySku {
  skuId: number
  specName: string
  price: number
  stock: number
}

// 拼团信息
export interface JoinableGroup {
  groupId: number
  leaderAvatar: string
  leaderNickname: string
  memberAvatars: string[]
  currentCount: number
  targetCount: number
  deadline: string
  remainingSeconds: number
}

// 活动详情
export interface ActivityDetail {
  id: number
  type: ActivityType
  title: string
  coverImages: string[]
  description: string
  originalPrice: number
  activityPrice: number
  stock: number
  soldCount: number
  startTime: string
  endTime: string
  status: ActivityStatus
  groupInfo?: {
    targetCount: number
    groupList: JoinableGroup[]
  }
  flashInfo?: {
    remainingSeconds: number
    soldPercent: number
  }
  skuList: ActivitySku[]
  serviceInfo: {
    address: string
    businessHours: string
    bookingPhone: string
  }
}

// 订单信息
export interface OrderInfo {
  orderId: string
  orderNo: string
  payAmount: number
  status: 'pending' | 'grouping' | 'success' | 'used' | 'refunded'
  statusText: string
  activityTitle: string
  activityCover: string
  petName?: string
  createTime: string
  payTime?: string
  wechatPayParams?: {
    timeStamp: string
    nonceStr: string
    package: string
    signType: string
    paySign: string
  }
}

// 会员次卡
export interface MemberCard {
  id: number
  cardName: string
  totalCount: number
  usedCount: number
  remainCount: number
  expireDate: string
  verifyCode: string
  qrCodeUrl: string
  barCodeUrl: string
}

// 服务提醒
export interface ServiceReminder {
  id: number
  petName: string
  type: 'vaccine' | 'deworming' | 'grooming'
  content: string
  date: string
}

// 聊天消息
export interface ChatMessage {
  id: number
  type: 'user' | 'ai'
  content: string
  timestamp: number
}

// 个人中心统计
export interface MemberStatistics {
  balance: number
  cardCount: number
  orderCount: number
  points: number
}
