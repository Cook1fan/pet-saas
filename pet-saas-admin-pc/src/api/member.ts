import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/utils/types'

// ==================== 会员相关类型 ====================

// 宠物信息（基于后端接口）
export interface PetInfo {
  id?: number
  createTime?: string
  createUser?: number
  updateTime?: string
  updateUser?: number
  isDeleted?: number
  tenantId?: number
  memberId?: number
  name: string
  breed?: string
  birthday?: string
  gender?: number  // 0-公，1-母
  vaccineTime?: string
  dewormTime?: string
  washTime?: string
  nextRemindTime?: string
}

// 会员信息（基于后端接口）
export interface MemberVO {
  id: number
  phone: string
  name: string
  tags?: string | null  // 多个用逗号分隔
  createTime: string
  pets?: PetInfo[]  // 会员列表也可能返回宠物信息
}

// 会员详情（含宠物列表）
export interface MemberDetailVO extends MemberVO {
  pets?: PetInfo[]
}

// 会员储值账户
export interface MemberAccountVO {
  id: number
  memberId: number
  balance: number
  totalRecharge: number
}

// 会员列表请求参数
export interface MemberListParams extends PageQuery {
  keyword?: string  // 手机号/姓名（模糊搜索）
  tag?: string      // 标签
}

// 会员创建请求
export interface CreateMemberReq {
  phone: string
  name: string
  tags?: string
  pets?: PetInfo[]
}

// 会员编辑请求
export interface UpdateMemberReq {
  id: number
  phone?: string
  name?: string
  tags?: string
  pets?: PetInfo[]
}

// ==================== 预设标签常量 ====================
export const PRESET_TAGS = [
  { value: 'VIP客户', color: 'danger' as const },
  { value: '新客户', color: 'primary' as const },
  { value: '养猫', color: 'success' as const },
  { value: '养狗', color: 'warning' as const },
  { value: '储值用户', color: 'info' as const },
  { value: '活跃客户', color: '' as const }
]

// 性别映射
export const GENDER_MAP: Record<number, string> = {
  0: '公',
  1: '母'
}

// ==================== 会员接口 ====================

// 获取会员列表
export function getMemberList(params: MemberListParams) {
  return request.get<PageResult<MemberVO>>('/pc/member/list', { params })
}

// 创建会员
export function createMember(data: CreateMemberReq) {
  return request.post<MemberVO>('/pc/member/create', data)
}

// 获取会员储值账户
export function getMemberAccount(memberId: number) {
  return request.get<MemberAccountVO>(`/pc/member/account/${memberId}`)
}

// 更新会员
export function updateMember(data: UpdateMemberReq) {
  return request.put<MemberVO>('/pc/member/update', data)
}

// TODO: 待后端补充接口
// export function deleteMember(id: number)
// export function getMemberDetail(id: number)
// export function batchImportMember(file: File)
// export function createPet(memberId: number, data: Partial<PetInfo>)
// export function updatePet(memberId: number, petId: number, data: Partial<PetInfo>)
// export function deletePet(memberId: number, petId: number)
