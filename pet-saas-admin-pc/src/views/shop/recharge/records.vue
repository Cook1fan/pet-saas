<template>
  <div class="recharge-records">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option value="recharge" label="储值" />
            <el-option value="card" label="次卡" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员">
          <el-input v-model="queryForm.memberName" placeholder="请输入会员姓名" clearable />
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
            <el-tag :type="row.type === 'recharge' ? 'success' : 'primary'">
              {{ row.type === 'recharge' ? '储值' : '次卡' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberName" label="会员姓名" />
        <el-table-column prop="memberPhone" label="会员手机号" />
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="amount" label="金额" v-if="!queryForm.type || queryForm.type === 'recharge'">
          <template #default="{ row }">
            <span v-if="row.amount" style="color: #f56c6c">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="times" label="次数" v-if="!queryForm.type || queryForm.type === 'card'">
          <template #default="{ row }">
            <span v-if="row.times">{{ row.times }}次</span>
          </template>
        </el-table-column>
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
import {
  getRechargeRecordList,
  type RechargeRecord,
  type RechargeRecordQuery
} from '@/api/recharge-card'

const loading = ref(false)
const tableData = ref<RechargeRecord[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const queryForm = reactive<RechargeRecordQuery>({
  page: 1,
  pageSize: 10,
  type: '',
  memberName: '',
  startTime: '',
  endTime: ''
})

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
    const res = await getRechargeRecordList(queryForm)
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
  queryForm.memberName = ''
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
.recharge-records {
  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
