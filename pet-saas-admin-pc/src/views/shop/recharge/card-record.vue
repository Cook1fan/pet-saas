<template>
  <div class="card-record">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="会员姓名">
          <el-input v-model="queryForm.memberName" placeholder="请输入会员姓名" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option value="purchase" label="购买" />
            <el-option value="verify" label="核销" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="queryForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="queryForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="memberName" label="会员姓名" />
        <el-table-column prop="cardName" label="次卡名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'purchase' ? 'primary' : 'success'">
              {{ row.type === 'purchase' ? '购买' : '核销' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="times" label="次数">
          <template #default="{ row }">
            <span :style="{ color: row.type === 'purchase' ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.type === 'purchase' ? '+' : '-' }}{{ row.times }}次
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remainTimes" label="剩余次数" />
        <el-table-column prop="operator" label="操作人" />
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
import { getCardRecordList } from '@/api/recharge-card'
import type { CardRecord } from '@/api/recharge-card'

const loading = ref(false)
const tableData = ref<CardRecord[]>([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  pageSize: 10,
  memberName: '',
  type: '',
  startTime: '',
  endTime: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getCardRecordList(queryForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.page = 1
  queryForm.memberName = ''
  queryForm.type = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.card-record {
  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
