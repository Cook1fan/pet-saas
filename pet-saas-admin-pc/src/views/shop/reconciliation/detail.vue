<template>
  <div class="reconciliation-detail">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option value="normal" label="正常" />
            <el-option value="abnormal" label="异常" />
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
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="systemAmount" label="系统金额" width="120">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: bold">¥{{ row.systemAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="wechatAmount" label="微信金额" width="120">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: bold">¥{{ row.wechatAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="diffAmount" label="差额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.diffAmount === 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              ¥{{ row.diffAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' ? 'success' : 'danger'">
              {{ row.status === 'normal' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getReconciliationList, type ReconciliationItem, type ReconciliationQuery } from '@/api/reconciliation'

const loading = ref(false)
const tableData = ref<ReconciliationItem[]>([])
const total = ref(0)

const queryForm = reactive<ReconciliationQuery>({
  page: 1,
  pageSize: 10,
  date: '',
  status: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getReconciliationList(queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.page = 1
  queryForm.date = ''
  queryForm.status = ''
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.reconciliation-detail {
  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
