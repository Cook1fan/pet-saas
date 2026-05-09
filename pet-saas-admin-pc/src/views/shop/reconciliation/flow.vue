<template>
  <div class="flow-record">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option value="income" label="收入" />
            <el-option value="expense" label="支出" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="queryForm.payType" placeholder="请选择支付方式" clearable style="width: 150px">
            <el-option value="wechat" label="微信支付" />
            <el-option value="cash" label="现金" />
            <el-option value="balance" label="余额" />
          </el-select>
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
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'income' ? 'success' : 'danger'">
              {{ row.type === 'income' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'income' ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.type === 'income' ? '+' : '-' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: bold">¥{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payType" label="支付方式" width="120">
          <template #default="{ row }">
            {{ getPayTypeText(row.payType) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单" width="180" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="时间" width="180" />
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
import { getFlowList, type FlowRecord, type FlowQuery } from '@/api/reconciliation'

const loading = ref(false)
const tableData = ref<FlowRecord[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const queryForm = reactive<FlowQuery>({
  page: 1,
  pageSize: 10,
  type: '',
  payType: ''
})

function getPayTypeText(payType?: string) {
  const map: Record<string, string> = { wechat: '微信支付', cash: '现金', balance: '余额' }
  return payType ? (map[payType] || payType) : '-'
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
    const res = await getFlowList(queryForm)
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
  queryForm.type = ''
  queryForm.payType = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  dateRange.value = []
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.flow-record {
  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
