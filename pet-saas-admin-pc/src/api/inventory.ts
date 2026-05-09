import request from '@/utils/request'

// ==================== 分页类型 ====================
export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// ==================== 分类相关类型 ====================
export interface Category {
  id: number
  parentId: number
  categoryName: string
  sort: number
  createTime: string
  children?: Category[]
}

// ==================== 商品 SKU 相关类型 ====================
export interface GoodsSku {
  id?: number
  goodsId?: number
  skuCode?: string
  specName: string
  specValue: string
  price: number
  costPrice: number
  stock?: number
  isUnlimitedStock?: number
  reservedStock?: number
  availableStock?: number
  barcode?: string
  status: number
  createTime?: string
  version?: number
}

// ==================== 商品相关类型 ====================
export interface Goods {
  id?: number
  tenantId?: number
  categoryId: number
  categoryName?: string
  goodsName: string
  mainImage?: string
  isService: number
  status: number
  createTime?: string
  version?: number
  skuList?: GoodsSku[]
}

export interface GoodsQuery extends PageQuery {
  keyword?: string
  categoryId?: number
  status?: number
}

// ==================== 库存记录相关类型 ====================
// 库存变动类型: 1-采购入库, 2-手动入库, 3-销售出库, 4-手动出库, 5-盘点调整, 6-退货入库, 7-领用出库
export type StockChangeType = 1 | 2 | 3 | 4 | 5 | 6 | 7

export interface StockRecordVO {
  id: number
  skuId: number
  type: StockChangeType
  changeNum: number
  beforeStock: number
  afterStock: number
  batchNo?: string
  relatedType?: string
  relatedId?: number
  relatedNo?: string
  remark?: string
  createTime: string
  createUser: number
  barcode?: string
  specName?: string
  specValue?: string
  goodsName?: string
  mainImage?: string
}

export interface StockRecordQuery extends PageQuery {
  skuId?: number
  type?: StockChangeType
  barcode?: string
  startTime?: string
  endTime?: string
}

// ==================== 库存操作请求类型 ====================
export interface StockManualInReq {
  skuId: number
  num: number
  remark?: string
}

export interface StockManualOutReq {
  skuId: number
  num: number
  remark?: string
}

export interface StockAdjustReq {
  skuId: number
  targetStock: number
  remark?: string
}

// ==================== 商品变更历史类型 ====================
// 数据类型: 1-goods商品, 2-goods_sku商品规格
export type ChangeLogDataType = 1 | 2
// 变更类型: 1-新增, 2-修改, 3-删除
export type ChangeLogType = 1 | 2 | 3

export interface ChangeDetail {
  fieldName: string
  fieldLabel: string
  beforeValue: string | null
  afterValue: string | null
  skuIdentifier: string | null
}

export interface GoodsChangeLogQuery extends PageQuery {
  dataType?: ChangeLogDataType
  dataId?: number
  batchNo?: string
}

export interface GoodsChangeLogVO {
  id: number
  dataType: ChangeLogDataType
  dataId: number
  changeType: ChangeLogType
  fieldName?: string
  beforeValue?: string
  afterValue?: string
  batchNo?: string
  remark?: string
  createTime: string
  createUser: number
  changeDetails?: ChangeDetail[]
  goodsChanges?: ChangeDetail[]
  skuChanges?: Record<string, ChangeDetail[]>
}

// ==================== 分类接口 ====================
export function getCategoryTree() {
  return request.get<Category[]>('/pc/goods-category/tree')
}

// ==================== 商品接口 ====================
export function getGoodsList(params: GoodsQuery) {
  return request.get<PageResult<Goods>>('/pc/goods/list', { params })
}

export function getGoods(goodsId: number) {
  return request.get<Goods>(`/pc/goods/${goodsId}`)
}

export function saveGoods(data: Partial<Goods>) {
  return request.post('/pc/goods/save', data)
}

export function updateGoodsStatus(goodsId: number, status: number) {
  return request.put(`/pc/goods/${goodsId}/status`, null, { params: { status } })
}

export function deleteGoods(goodsId: number) {
  return request.delete(`/pc/goods/${goodsId}`)
}

// ==================== 商品变更历史接口 ====================
export function getChangeLog(params: GoodsChangeLogQuery) {
  return request.get<PageResult<GoodsChangeLogVO>>('/pc/goods/change-log', { params })
}

// ==================== 库存预警接口 ====================
export function getWarnList() {
  return request.get<GoodsSku[]>('/pc/goods/warnList')
}

// ==================== 库存管理接口 ====================
export function manualInStock(data: StockManualInReq) {
  return request.post<StockRecordVO>('/pc/stock/in', data)
}

export function manualOutStock(data: StockManualOutReq) {
  return request.post<StockRecordVO>('/pc/stock/out', data)
}

export function adjustStock(data: StockAdjustReq) {
  return request.post<StockRecordVO>('/pc/stock/adjust', data)
}

export function listStockRecords(params: StockRecordQuery) {
  return request.get<PageResult<StockRecordVO>>('/pc/stock/records', { params })
}

// ==================== 辅助函数 ====================
// 库存变动类型映射
export const STOCK_TYPE_MAP: Record<StockChangeType, { label: string; type: 'success' | 'danger' | 'warning' | 'info' }> = {
  1: { label: '采购入库', type: 'success' },
  2: { label: '手动入库', type: 'success' },
  3: { label: '销售出库', type: 'danger' },
  4: { label: '手动出库', type: 'danger' },
  5: { label: '盘点调整', type: 'warning' },
  6: { label: '退货入库', type: 'success' },
  7: { label: '领用出库', type: 'danger' }
}

// 变更类型映射
export const CHANGE_TYPE_MAP: Record<ChangeLogType, string> = {
  1: '新增',
  2: '修改',
  3: '删除'
}

// 数据类型映射
export const DATA_TYPE_MAP: Record<ChangeLogDataType, string> = {
  1: '商品',
  2: '商品规格'
}
