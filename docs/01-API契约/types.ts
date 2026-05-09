/**
 * 宠物门店 SaaS - 共用 TypeScript 类型定义
 *
 * 🔴 重要：此文件是前后端类型的单一真实来源（SSOT）
 * - 后端：可用工具生成 Java DTO
 * - 前端：直接用于 TypeScript 类型检查
 */

// ============================================================================
// 通用类型
// ============================================================================

/**
 * 统一 API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页请求参数
 */
export interface PageRequest {
  page: number
  pageSize: number
  condition?: Record<string, any>
}

/**
 * 分页响应数据
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

// ============================================================================
// 认证相关类型
// ============================================================================

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
  tenantId?: number
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  role: 'platform_admin' | 'shop_admin' | 'shop_staff'
  tenantId?: number
}

// ============================================================================
// 租户相关类型
// ============================================================================

/**
 * 租户信息
 */
export interface TenantInfo {
  tenantId: number
  shopName: string
  adminPhone: string
  address: string
  status: 0 | 1
  createTime: string
}

// ============================================================================
// 会员相关类型
// ============================================================================

/**
 * 会员信息
 */
export interface MemberInfo {
  id: number
  tenantId: number
  phone: string
  name: string
  tags: string[]
  createTime: string
}

/**
 * 宠物信息
 */
export interface PetInfo {
  id: number
  tenantId: number
  memberId: number
  name: string
  breed: string
  birthday?: string
  vaccineTime?: string
  dewormTime?: string
  washTime?: string
}

// ============================================================================
// 商品相关类型
// ============================================================================

/**
 * 商品分类枚举
 */
export enum GoodsCategory {
  FOOD = 'food',           // 宠物食品类
  SUPPLIES = 'supplies',   // 宠物用品类
  SERVICE = 'service',     // 宠物服务类
  MEDICAL = 'medical'      // 宠物医疗类
}

/**
 * 商品信息
 */
export interface GoodsInfo {
  id: number
  tenantId: number
  name: string
  category: GoodsCategory
  price: number
  stock: number
  warnStock: number
  isService: boolean
  status: 0 | 1
}

// ============================================================================
// 订单相关类型
// ============================================================================

/**
 * 订单信息
 */
export interface OrderInfo {
  id: number
  tenantId: number
  orderNo: string
  memberId?: number
  totalAmount: number
  payAmount: number
  payType: 'wechat' | 'cash' | 'recharge' | 'card'
  payStatus: 0 | 1 | 2
  payTime?: string
  transactionId?: string
  createTime: string
}

/**
 * 订单明细
 */
export interface OrderItem {
  id: number
  tenantId: number
  orderId: number
  goodsId: number
  goodsName: string
  num: number
  price: number
}

// ============================================================================
// 活动相关类型
// ============================================================================

/**
 * 活动类型枚举
 */
export enum ActivityType {
  GROUPON = 'groupon',   // 拼团
  SECKILL = 'seckill'    // 秒杀
}

/**
 * 活动信息
 */
export interface ActivityInfo {
  id: number
  tenantId: number
  type: ActivityType
  title: string
  goodsId: number
  price: number
  originPrice: number
  stock: number
  startTime: string
  endTime: string
  status: 0 | 1
}

// ============================================================================
// 储值/次卡相关类型
// ============================================================================

/**
 * 储值规则
 */
export interface RechargeRule {
  id: number
  tenantId: number
  name: string
  rechargeAmount: number
  giveAmount: number
  status: 0 | 1
}

/**
 * 次卡规则
 */
export interface CardRule {
  id: number
  tenantId: number
  name: string
  times: number
  price: number
  validDays: number
  status: 0 | 1
}

/**
 * 会员账户
 */
export interface MemberAccount {
  id: number
  tenantId: number
  memberId: number
  balance: number
  totalRecharge: number
  createTime: string
}

/**
 * 会员次卡
 */
export interface MemberCard {
  id: number
  tenantId: number
  memberId: number
  cardRuleId: number
  remainTimes: number
  expireTime?: string
}
