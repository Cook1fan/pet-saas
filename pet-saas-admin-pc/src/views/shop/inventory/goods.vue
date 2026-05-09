<template>
  <div class="goods-list">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.keyword" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="categoryQueryValue"
            :options="categoryOptions"
            :props="cascaderProps"
            placeholder="请选择分类"
            clearable
            change-on-select
            @change="handleCategoryQueryChange"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 100px">
            <el-option :value="1" label="上架" />
            <el-option :value="0" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增商品
        </el-button>
        <el-button @click="goToStockRecord">
          <el-icon><Document /></el-icon>
          库存记录
        </el-button>
      </div>
      <el-table :data="filteredTableData" stripe v-loading="loading" row-key="id">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="sku-expand">
              <div class="sku-expand-header">
                <span class="sku-expand-title">规格库存明细</span>
                <span class="sku-expand-count">共 {{ row.skuList?.length || 0 }} 个规格</span>
              </div>
              <div class="sku-table-wrapper">
                <el-table :data="row.skuList" border v-if="row.skuList && row.skuList.length > 0" class="sku-table" size="small">
                  <el-table-column label="编码" width="80">
                    <template #default="{ row }">
                      <el-tag type="info" size="small">{{ row.id }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="barcode" label="商品条码" min-width="160">
                    <template #default="{ row }">
                      <span>{{ row.barcode || '-' }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="specValue" label="规格值" min-width="150">
                    <template #default="{ row }">
                      <div class="spec-value-cell">
                        <span>{{ row.specValue }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="price" label="售价" width="130">
                    <template #default="{ row }">
                      <span class="price-tag">¥{{ row.price.toFixed(2) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="stock" label="当前库存" width="140">
                    <template #default="{ row }">
                      <span v-if="row.isUnlimitedStock === 1">
                        <el-tag type="warning" size="small">♾️</el-tag>
                      </span>
                      <span v-else style="display: flex; align-items: center; gap: 6px">
                        <el-tag type="success" size="small">{{ row.stock }}</el-tag>
                        <el-icon
                          v-if="row.barcode"
                          class="view-stock-icon"
                          @click="viewStockRecord(row)"
                          style="cursor: pointer; color: #409eff"
                        >
                          <View />
                        </el-icon>
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="reservedStock" label="占用库存" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.isUnlimitedStock === 1" type="info" size="small">-</el-tag>
                      <el-tag v-else type="warning" size="small">{{ row.reservedStock || 0 }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="availableStock" label="可用库存" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.isUnlimitedStock === 1" type="warning" size="small">♾️</el-tag>
                      <el-tag v-else type="primary" size="small">{{ row.availableStock ?? (row.stock ?? 0) - (row.reservedStock ?? 0) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="90">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                        {{ row.status === 1 ? '启用' : '禁用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row: sku, $index }">
                      <div v-if="sku.isUnlimitedStock !== 1" class="action-buttons">
                        <el-button type="success" size="small" link @click="handleInStock(sku, row)" class="action-btn-compact">
                          入库
                        </el-button>
                        <el-button type="warning" size="small" link @click="handleOutStock(sku, row)" class="action-btn-compact">
                          出库
                        </el-button>
                        <el-button type="primary" size="small" link @click="handleAdjustStock(sku, row)" class="action-btn-compact">
                          盘点
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-else description="暂无规格" />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品图片" width="90">
          <template #default="{ row }">
            <img
              v-if="row.mainImage"
              v-viewer="row.mainImage"
              :src="row.mainImage"
              class="goods-image"
              alt="商品图片"
            />
            <el-icon v-else style="font-size: 40px; color: #ccc"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column label="规格库存" width="180">
          <template #default="{ row }">
            <div v-if="row.skuList && row.skuList.length > 0" class="sku-info-compact">
              <div class="sku-summary">
                <el-tag :type="getOverallStockStatus(row)" size="small">
                  {{ getOverallStockLabel(row) }}
                </el-tag>
                <span class="sku-count">{{ row.skuList.length }}规格</span>
              </div>
              <div class="sku-price-row">
                <span class="sku-price">¥{{ getMinPrice(row.skuList) }}~¥{{ getMaxPrice(row.skuList) }}</span>
              </div>
            </div>
            <el-tag v-else type="info" size="small">暂无规格</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="handleStatusChange(row)"
              :loading="row.statusLoading"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="info" link @click="showChangeLog(row)">
              <el-icon><Document /></el-icon>
              变更历史
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 变更历史抽屉 -->
    <ChangeLogDrawer v-model="changeLogDrawerVisible" :data-id="currentChangeLogDataId" />

    <!-- 入库对话框 -->
    <el-dialog v-model="inStockDialogVisible" title="手动入库" width="500px">
      <el-form :model="inStockForm" label-width="100px">
        <el-form-item label="商品">
          <span class="form-value">{{ inStockForm.goodsName }}</span>
        </el-form-item>
        <el-form-item label="规格">
          <span class="form-value">{{ inStockForm.specValue }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag>{{ inStockForm.currentStock }}</el-tag>
        </el-form-item>
        <el-form-item label="入库数量" required>
          <el-input-number v-model="inStockForm.num" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入库后库存">
          <el-tag type="success">{{ (inStockForm.currentStock || 0) + (inStockForm.num || 0) }}</el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="inStockForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inStockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInStock" :loading="submitLoading">确定入库</el-button>
      </template>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog v-model="outStockDialogVisible" title="手动出库" width="500px">
      <el-form :model="outStockForm" label-width="100px">
        <el-form-item label="商品">
          <span class="form-value">{{ outStockForm.goodsName }}</span>
        </el-form-item>
        <el-form-item label="规格">
          <span class="form-value">{{ outStockForm.specValue }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag>{{ outStockForm.currentStock }}</el-tag>
        </el-form-item>
        <el-form-item label="出库数量" required>
          <el-input-number
            v-model="outStockForm.num"
            :min="1"
            :max="outStockForm.currentStock"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="出库后库存">
          <el-tag type="warning">{{ Math.max(0, (outStockForm.currentStock || 0) - (outStockForm.num || 0)) }}</el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="outStockForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outStockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOutStock" :loading="submitLoading">确定出库</el-button>
      </template>
    </el-dialog>

    <!-- 盘点对话框 -->
    <el-dialog v-model="adjustStockDialogVisible" title="盘点调整" width="500px">
      <el-form :model="adjustStockForm" label-width="100px">
        <el-form-item label="商品">
          <span class="form-value">{{ adjustStockForm.goodsName }}</span>
        </el-form-item>
        <el-form-item label="规格">
          <span class="form-value">{{ adjustStockForm.specValue }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag>{{ adjustStockForm.currentStock }}</el-tag>
        </el-form-item>
        <el-form-item label="盘点后库存" required>
          <el-input-number v-model="adjustStockForm.targetStock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="变动数量">
          <el-tag :type="getAdjustChangeType()">
            {{ getAdjustChangeLabel() }}
          </el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustStockForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustStockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjustStock" :loading="submitLoading">确定调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Picture, Document, View } from '@element-plus/icons-vue'
import {
  getGoodsList,
  deleteGoods,
  getCategoryTree,
  updateGoodsStatus,
  manualInStock,
  manualOutStock,
  adjustStock,
  type Goods,
  type GoodsQuery,
  type GoodsSku
} from '@/api/inventory'
import ChangeLogDrawer from '@/components/ChangeLogDrawer/index.vue'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Goods[]>([])
const total = ref(0)
const currentPage = ref(1)
const categoryOptions = ref<any[]>([])
const categoryQueryValue = ref<number[]>([])

// 变更历史相关
const changeLogDrawerVisible = ref(false)
const currentChangeLogDataId = ref<number | null>(null)

// 入库对话框
const inStockDialogVisible = ref(false)
const inStockForm = reactive({
  skuId: 0,
  goodsName: '',
  specValue: '',
  currentStock: 0,
  num: 1,
  remark: ''
})

// 出库对话框
const outStockDialogVisible = ref(false)
const outStockForm = reactive({
  skuId: 0,
  goodsName: '',
  specValue: '',
  currentStock: 0,
  num: 1,
  remark: ''
})

// 盘点对话框
const adjustStockDialogVisible = ref(false)
const adjustStockForm = reactive({
  skuId: 0,
  goodsName: '',
  specValue: '',
  currentStock: 0,
  targetStock: 0,
  remark: ''
})

const cascaderProps = {
  value: 'id',
  label: 'categoryName',
  children: 'children',
  checkStrictly: true,
  emitPath: false
}

const queryForm = reactive<GoodsQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined,
  status: undefined
})

const filteredTableData = computed(() => tableData.value)

async function loadData() {
  loading.value = true
  try {
    queryForm.pageNum = currentPage.value
    const res = await getGoodsList(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categoryOptions.value = await getCategoryTree()
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

function handleCategoryQueryChange(value: number) {
  queryForm.categoryId = value
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function resetQuery() {
  currentPage.value = 1
  queryForm.pageNum = 1
  queryForm.keyword = ''
  queryForm.categoryId = undefined
  queryForm.status = undefined
  categoryQueryValue.value = []
  loadData()
}

function handleCreate() {
  router.push('/shop/goods/create')
}

function handleEdit(row: Goods) {
  router.push(`/shop/goods/edit/${row.id}`)
}

function goToStockRecord() {
  router.push('/shop/goods/stock-record')
}

async function handleStatusChange(row: Goods) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    ;(row as any).statusLoading = true
    await updateGoodsStatus(row.id!, newStatus)
    row.status = newStatus
    ElMessage.success('操作成功')
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    ;(row as any).statusLoading = false
  }
}

async function handleDelete(row: Goods) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteGoods(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function getMinPrice(skuList: GoodsSku[]): string {
  if (!skuList || skuList.length === 0) return '0'
  const minPrice = Math.min(...skuList.map(s => s.price))
  return minPrice.toFixed(2)
}

function getMaxPrice(skuList: GoodsSku[]): string {
  if (!skuList || skuList.length === 0) return '0'
  const maxPrice = Math.max(...skuList.map(s => s.price))
  return maxPrice.toFixed(2)
}

function getTotalStock(goods: Goods): number {
  if (!goods.skuList || goods.skuList.length === 0) return 0
  return goods.skuList.reduce((sum, s) => sum + s.stock, 0)
}

function getStockStatus(goods: Goods): string {
  if (!goods.skuList || goods.skuList.length === 0) return ''
  const hasOutStock = goods.skuList.some(s => s.stock === 0)
  if (hasOutStock) return 'danger'
  return 'success'
}

function getOverallStockStatus(goods: Goods): string {
  if (!goods.skuList || goods.skuList.length === 0) return 'info'
  const hasOutStock = goods.skuList.some(s => s.stock === 0)
  if (hasOutStock) return 'danger'
  return 'success'
}

function getOverallStockLabel(goods: Goods): string {
  if (!goods.skuList || goods.skuList.length === 0) return '暂无规格'
  const hasOutStock = goods.skuList.some(s => s.stock === 0)
  if (hasOutStock) return '部分缺货'
  return '库存正常'
}

function showChangeLog(goods: Goods) {
  if (!goods.id) return
  currentChangeLogDataId.value = goods.id
  changeLogDrawerVisible.value = true
}

function viewStockRecord(sku: GoodsSku) {
  if (sku.barcode) {
    const routeUrl = router.resolve({
      path: '/shop/goods/stock-record',
      query: { barcode: sku.barcode }
    })
    window.open(routeUrl.href, '_blank')
  }
}

// 库存操作相关函数
function handleInStock(sku: GoodsSku, goods: Goods) {
  Object.assign(inStockForm, {
    skuId: sku.id || 0,
    goodsName: goods.goodsName,
    specValue: sku.specValue,
    currentStock: sku.stock,
    num: 1,
    remark: ''
  })
  inStockDialogVisible.value = true
}

async function confirmInStock() {
  if (!inStockForm.num || inStockForm.num < 1) {
    ElMessage.warning('请输入入库数量')
    return
  }
  submitLoading.value = true
  try {
    await manualInStock({
      skuId: inStockForm.skuId,
      num: inStockForm.num,
      remark: inStockForm.remark
    })
    ElMessage.success('入库成功')
    inStockDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('入库失败')
  } finally {
    submitLoading.value = false
  }
}

function handleOutStock(sku: GoodsSku, goods: Goods) {
  Object.assign(outStockForm, {
    skuId: sku.id || 0,
    goodsName: goods.goodsName,
    specValue: sku.specValue,
    currentStock: sku.stock,
    num: 1,
    remark: ''
  })
  outStockDialogVisible.value = true
}

async function confirmOutStock() {
  if (!outStockForm.num || outStockForm.num < 1) {
    ElMessage.warning('请输入出库数量')
    return
  }
  if (outStockForm.num > outStockForm.currentStock) {
    ElMessage.warning('出库数量不能超过当前库存')
    return
  }
  submitLoading.value = true
  try {
    await manualOutStock({
      skuId: outStockForm.skuId,
      num: outStockForm.num,
      remark: outStockForm.remark
    })
    ElMessage.success('出库成功')
    outStockDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('出库失败')
  } finally {
    submitLoading.value = false
  }
}

function handleAdjustStock(sku: GoodsSku, goods: Goods) {
  Object.assign(adjustStockForm, {
    skuId: sku.id || 0,
    goodsName: goods.goodsName,
    specValue: sku.specValue,
    currentStock: sku.stock,
    targetStock: sku.stock,
    remark: ''
  })
  adjustStockDialogVisible.value = true
}

function getAdjustChangeType(): string {
  const diff = adjustStockForm.targetStock - adjustStockForm.currentStock
  if (diff > 0) return 'success'
  if (diff < 0) return 'danger'
  return 'info'
}

function getAdjustChangeLabel(): string {
  const diff = adjustStockForm.targetStock - adjustStockForm.currentStock
  if (diff > 0) return `+${diff}`
  if (diff < 0) return `${diff}`
  return '无变化'
}

async function confirmAdjustStock() {
  submitLoading.value = true
  try {
    await adjustStock({
      skuId: adjustStockForm.skuId,
      targetStock: adjustStockForm.targetStock,
      remark: adjustStockForm.remark
    })
    ElMessage.success('盘点调整成功')
    adjustStockDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('盘点调整失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped lang="scss">
.goods-list {
  .search-form {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .goods-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    object-fit: cover;
    cursor: pointer;
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.05);
    }
  }

  .sku-summary {
    display: flex;
    gap: 8px;
    margin-bottom: 4px;
    align-items: center;

    .sku-count {
      color: #409eff;
      font-weight: bold;
      font-size: 13px;
    }
  }

  .sku-price {
    color: #f56c6c;
    font-weight: bold;
    font-size: 13px;
    margin-bottom: 4px;
  }

  .sku-list {
    line-height: 1.5;
  }

  .price {
    color: #f56c6c;
    font-weight: bold;
  }

  .sku-info-compact {
    .sku-summary {
      display: flex;
      gap: 6px;
      margin-bottom: 4px;
      align-items: center;
      flex-wrap: wrap;

      .sku-count {
        color: #409eff;
        font-weight: 500;
        font-size: 12px;
      }
    }

    .sku-price-row {
      .sku-price {
        color: #f56c6c;
        font-weight: 600;
        font-size: 13px;
      }
    }
  }

  :deep(.el-table__expanded-cell) {
    padding: 0 !important;
    background: #f8fafc !important;
  }

  .sku-expand {
    padding: 0;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;

    .sku-expand-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 20px;
      background: linear-gradient(90deg, #f1f5f9 0%, #f8fafc 100%);
      border-bottom: 1px solid #e2e8f0;

      .sku-expand-title {
        font-size: 13px;
        font-weight: 600;
        color: #475569;
        display: flex;
        align-items: center;
        gap: 8px;

        &::before {
          content: '';
          width: 4px;
          height: 12px;
          background: linear-gradient(180deg, #409eff 0%, #79bbff 100%);
          border-radius: 2px;
        }
      }

      .sku-expand-count {
        font-size: 12px;
        color: #64748b;
        background: #e2e8f0;
        padding: 3px 10px;
        border-radius: 10px;
        font-weight: 500;
      }
    }

    .sku-table-wrapper {
      padding: 16px 20px;
    }

    .sku-table {
      :deep(.el-table__header-wrapper) {
        th {
          background: #f1f5f9;
          color: #64748b;
          font-weight: 600;
          font-size: 12px;
          padding: 8px 0;
        }
      }

      :deep(.el-table__body-wrapper) {
        td {
          padding: 8px 0;
        }

        tr {
          background: transparent;

          &:hover > td {
            background: #f1f5f9;
          }
        }
      }

      :deep(.el-table--border) {
        border: 1px solid #e2e8f0;
        border-radius: 6px;
        overflow: hidden;
      }

      :deep(.el-table--border::after),
      :deep(.el-table--group::after) {
        background-color: #e2e8f0;
      }

      :deep(.el-table td),
      :deep(.el-table th.is-leaf) {
        border-bottom: 1px solid #e2e8f0;
      }

      :deep(.el-table--border td),
      :deep(.el-table--border th) {
        border-right: 1px solid #e2e8f0;
      }
    }

    .spec-value-cell {
      display: flex;
      align-items: center;

      span {
        font-size: 13px;
        font-weight: 500;
        color: #334155;
      }
    }

    .price-tag {
      font-size: 14px;
      font-weight: 600;
      color: #f56c6c;
    }

    .action-buttons {
      display: flex;
      gap: 2px;

      .action-btn {
        font-size: 13px;
        padding: 6px 10px;
        border-radius: 4px;
        font-weight: 500;

        .el-icon {
          margin-right: 2px;
        }
      }

      .action-btn-compact {
        font-size: 12px;
        padding: 4px 8px;
        border-radius: 3px;
        font-weight: 500;
      }
    }
  }

  .form-value {
    color: #606266;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
