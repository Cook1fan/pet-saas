<template>
  <el-drawer v-model="visibleProxy" size="50%" @close="handleClose">
    <template #header>
      <div class="drawer-header">
        <span class="drawer-title">变更历史</span>
      </div>
    </template>
    <div class="change-log-scroll-wrapper" ref="scrollWrapperRef" @scroll="handleScroll">
      <el-timeline>
        <el-skeleton v-if="loading && logs.length === 0" :rows="3" animated />
        <template v-else>
          <el-timeline-item
            v-for="log in logs"
            :key="log.id"
            :timestamp="log.createTime"
            placement="top"
          >
            <el-card>
              <div class="change-log-item">
                <div class="change-log-header">
                  <el-tag :type="getChangeLogTagType(log.changeType)">{{ getChangeTypeLabel(log.changeType) }}</el-tag>
                </div>
                <div class="change-details-wrapper">
                  <div v-if="hasNewStructure(log)" class="change-details">
                    <el-table :data="getMergedTableRows(log)" border style="width: 100%" size="small" :span-method="getSpanMethod">
                      <el-table-column label="字段" min-width="150">
                        <template #default="{ row }">
                          <div class="field-cell">
                            <span v-if="row.isSkuHeader" class="sku-header">{{ row.fieldLabel }}</span>
                            <span v-else class="field-label">{{ row.fieldLabel }}</span>
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column label="之前" min-width="200">
                        <template #default="{ row }">
                          <span v-if="!row.isSkuHeader" class="value-text before">{{ formatDisplayValue(row.beforeValue) }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column label="之后" min-width="200">
                        <template #default="{ row }">
                          <span v-if="!row.isSkuHeader" class="value-text after">{{ formatDisplayValue(row.afterValue) }}</span>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                  <div v-else class="change-details">
                    <div v-if="getFilteredChangeDetails(log).length > 0">
                      <el-table :data="getFilteredChangeDetails(log)" size="small" border style="width: 100%">
                        <el-table-column label="字段">
                          <template #default="{ row }">
                            <div class="field-cell">
                              <span class="field-label">{{ row.fieldLabel }}</span>
                              <span v-if="row.skuIdentifier" class="sku-tag">{{ row.skuIdentifier }}</span>
                            </div>
                          </template>
                        </el-table-column>
                        <el-table-column label="之前">
                          <template #default="{ row }">
                            <span class="value-text before">{{ formatDisplayValue(row.beforeValue) }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column label="之后">
                          <template #default="{ row }">
                            <span class="value-text after">{{ formatDisplayValue(row.afterValue) }}</span>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                    <div v-else class="change-log-detail">
                      {{ log.changeType === 1 ? '新增数据' : log.changeType === 3 ? '删除数据' : '修改数据' }}
                    </div>
                  </div>
                </div>
                <div v-if="log.remark" class="change-log-remark">
                  备注: {{ log.remark }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </template>
      </el-timeline>
      <el-empty v-if="logs.length === 0 && !loading" description="暂无变更记录" />
      <div v-if="loading && logs.length > 0" class="loading-more">
        <el-skeleton :rows="2" animated />
      </div>
      <div v-if="!hasMore && logs.length > 0" class="no-more">
        <span>没有更多了</span>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getChangeLog,
  type GoodsChangeLogVO,
  type ChangeDetail,
  CHANGE_TYPE_MAP
} from '@/api/inventory'

interface Props {
  modelValue: boolean
  dataId: number | null
  dataType?: 1 | 2
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  dataType: 1,
  pageSize: 20
})

const emit = defineEmits(['update:modelValue'])

const visibleProxy = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const logs = ref<GoodsChangeLogVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const loading = ref(false)
const hasMore = ref(false)
const scrollWrapperRef = ref<HTMLElement | null>(null)

interface MergedTableRow {
  fieldName?: string
  fieldLabel: string
  beforeValue?: string | null
  afterValue?: string | null
  isSkuHeader?: boolean
  skuId?: string
}

async function loadData(reset = true) {
  if (!props.dataId) return
  loading.value = true
  try {
    const res = await getChangeLog({
      dataType: props.dataType,
      dataId: props.dataId,
      pageNum: currentPage.value,
      pageSize: props.pageSize
    })
    if (reset) {
      logs.value = res.records || []
    } else {
      logs.value = [...logs.value, ...(res.records || [])]
    }
    total.value = res.total || 0
    hasMore.value = logs.value.length < total.value
  } catch (e) {
    ElMessage.error('加载变更历史失败')
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loading.value || !hasMore.value) return
  currentPage.value++
  await loadData(false)
}

function handleScroll(evt: Event) {
  const target = evt.target as HTMLElement
  if (!target) return

  const { scrollTop, scrollHeight, clientHeight } = target
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    loadMore()
  }
}

function reset() {
  logs.value = []
  total.value = 0
  currentPage.value = 1
  hasMore.value = false
}

function handleClose() {
  reset()
}

watch(
  () => props.modelValue,
  async (val) => {
    if (val && props.dataId) {
      reset()
      await loadData()
    }
  }
)

function getChangeTypeLabel(type: number): string {
  return CHANGE_TYPE_MAP[type as keyof typeof CHANGE_TYPE_MAP] || '未知'
}

function getChangeLogTagType(type: number): string {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[type] || 'info'
}

function formatDisplayValue(value: string | null | undefined): string {
  if (value === null || value === undefined) return '-'
  return value
}

function getFilteredChangeDetails(log: any) {
  if (!log.changeDetails || log.changeDetails.length === 0) return []
  return log.changeDetails
}

function hasNewStructure(log: GoodsChangeLogVO): boolean {
  return !!(log.goodsChanges || log.skuChanges)
}

function getSkuLabel(log: GoodsChangeLogVO, skuId: string): string {
  return `规格编码: ${skuId}`
}

function getMergedTableRows(log: GoodsChangeLogVO): MergedTableRow[] {
  const rows: MergedTableRow[] = []

  // 添加商品级别的变更
  if (log.goodsChanges) {
    log.goodsChanges.forEach(detail => {
      rows.push({
        fieldName: detail.fieldName,
        fieldLabel: detail.fieldLabel,
        beforeValue: detail.beforeValue,
        afterValue: detail.afterValue
      })
    })
  }

  // 添加 SKU 级别的变更
  if (log.skuChanges) {
    Object.keys(log.skuChanges).forEach(skuId => {
      const skuChanges = log.skuChanges![skuId]
      // 添加 SKU 头行（合并单元格）
      rows.push({
        fieldLabel: getSkuLabel(log, skuId),
        isSkuHeader: true,
        skuId
      })
      // 添加 SKU 字段变更行
      skuChanges.forEach(detail => {
        rows.push({
          fieldName: detail.fieldName,
          fieldLabel: detail.fieldLabel,
          beforeValue: detail.beforeValue,
          afterValue: detail.afterValue,
          skuId
        })
      })
    })
  }

  return rows
}

function getSpanMethod({ row, column, rowIndex, columnIndex }: any) {
  if (row.isSkuHeader) {
    if (columnIndex === 0) {
      return {
        colspan: 3,
        rowspan: 1
      }
    } else {
      return {
        colspan: 0,
        rowspan: 0
      }
    }
  }
}
</script>

<style scoped lang="scss">
:deep(.el-drawer__header) {
  margin: 0;
  padding: 16px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.drawer-header {
  .drawer-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.change-log-scroll-wrapper {
  height: calc(100vh - 60px);
  overflow-y: auto;
  padding-right: 8px;

  .loading-more {
    padding: 16px 0;
  }

  .no-more {
    text-align: center;
    padding: 16px 0;
    color: #909399;
    font-size: 13px;
  }
}

:deep(.el-card) {
  .el-card__body {
    padding: 0;
  }
}

.change-log-item {
  padding: 0;

  .change-log-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    padding: 12px 16px;
    background: #f5f7fa;
    border-bottom: 1px solid #e4e7ed;
  }

  .change-log-detail {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
  }

  .change-log-remark {
    color: #909399;
    font-size: 12px;
    padding: 0 16px 16px 16px;
  }

  .change-details-wrapper {
    width: 100%;
  }

  .two-dimensional-table {
    width: 100%;
    overflow-x: auto;
    padding: 16px;

    :deep(.el-table) {
      font-size: 13px;
      width: 100%;
    }

    .field-cell {
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 6px;

      .field-label {
        font-weight: 500;
        color: #409eff;
      }
    }

    .change-cell {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .arrow {
        color: #909399;
        font-weight: bold;
      }
    }

    .no-change {
      color: #c0c4cc;
    }
  }

  .change-details {
    width: 100%;
    padding: 16px;

    :deep(.el-table) {
      font-size: 13px;
      width: 100%;
    }

    .field-cell {
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;

      .field-label {
        font-weight: 500;
        color: #409eff;
      }

      .sku-header {
        font-weight: 600;
        color: #303133;
        background: #f5f7fa;
        padding: 4px 12px;
        border-radius: 4px;
      }

      .sku-tag {
        font-size: 11px;
        color: #909399;
        background: #f5f7fa;
        padding: 1px 4px;
        border-radius: 3px;
      }
    }

    .value-text {
      word-break: break-all;

      &.before {
        color: #f56c6c;
        text-decoration: line-through;
      }

      &.after {
        color: #67c23a;
        font-weight: 500;
      }
    }
  }
}
</style>
