<template>
  <div class="stock-record">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="变动类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 180px">
            <el-option v-for="(item, key) in STOCK_TYPE_MAP" :key="key" :value="Number(key)" :label="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品条码">
          <el-input v-model="queryForm.barcode" placeholder="请输入商品条码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="goods-info-cell">
              <img v-if="row.mainImage" :src="row.mainImage" class="goods-image" alt="商品图片" />
              <el-icon v-else class="goods-image-placeholder"><Picture /></el-icon>
              <div class="goods-detail">
                <div class="goods-name">{{ row.goodsName || '-' }}</div>
                <div class="goods-spec">
                  <span v-if="row.specName">{{ row.specName }}</span>
                  <span v-if="row.specName && row.specValue">：</span>
                  <span v-if="row.specValue">{{ row.specValue }}</span>
                </div>
                <div class="goods-barcode" v-if="row.barcode">
                  <el-tag size="small" type="info">条码: {{ row.barcode }}</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="变动类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getStockTypeInfo(row.type).type">
              {{ getStockTypeInfo(row.type).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="变动前库存" width="120" />
        <el-table-column label="变动数量" width="120">
          <template #default="{ row }">
            <span :class="{ 'num-in': row.changeNum > 0, 'num-out': row.changeNum < 0 }">
              {{ row.changeNum > 0 ? '+' : '' }}{{ row.changeNum }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="afterStock" label="变动后库存" width="120" />
        <el-table-column label="关联单据" width="180">
          <template #default="{ row }">
            <div v-if="row.relatedNo">
              <div class="related-info">
                <span class="related-type">{{ row.relatedType || '-' }}</span>
                <span class="related-no">{{ row.relatedNo }}</span>
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="180" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import {
  listStockRecords,
  type StockRecordVO,
  type StockRecordQuery,
  type StockChangeType,
  STOCK_TYPE_MAP
} from '@/api/inventory'

const route = useRoute()
const loading = ref(false)
const tableData = ref<StockRecordVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const dateRange = ref<string[]>([])

const queryForm = reactive<StockRecordQuery>({
  pageNum: 1,
  pageSize: 10,
  type: undefined,
  barcode: '',
  startTime: '',
  endTime: ''
})

function getStockTypeInfo(type: StockChangeType) {
  return STOCK_TYPE_MAP[type] || { label: '未知', type: 'info' }
}

async function loadData() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryForm.startTime = dateRange.value[0]
      queryForm.endTime = dateRange.value[1]
    } else {
      queryForm.startTime = ''
      queryForm.endTime = ''
    }
    queryForm.pageNum = currentPage.value
    const res = await listStockRecords(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function resetQuery() {
  currentPage.value = 1
  queryForm.pageNum = 1
  queryForm.type = undefined
  queryForm.barcode = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  dateRange.value = []
  loadData()
}

onMounted(() => {
  if (route.query.barcode) {
    queryForm.barcode = route.query.barcode as string
  }
  loadData()
})
</script>

<style scoped lang="scss">
.stock-record {
  .search-form {
    margin-bottom: 16px;
  }

  .goods-info-cell {
    display: flex;
    align-items: center;
    gap: 12px;

    .goods-image {
      width: 50px;
      height: 50px;
      border-radius: 4px;
      object-fit: cover;
      flex-shrink: 0;
    }

    .goods-image-placeholder {
      width: 50px;
      height: 50px;
      color: #ccc;
      flex-shrink: 0;
    }

    .goods-detail {
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;

      .goods-name {
        font-weight: 500;
        color: #303133;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .goods-spec {
        font-size: 12px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .num-in {
    color: #67c23a;
    font-weight: bold;
  }

  .num-out {
    color: #f56c6c;
    font-weight: bold;
  }

  .related-info {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .related-type {
      font-size: 12px;
      color: #909399;
    }

    .related-no {
      font-weight: 500;
    }
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
