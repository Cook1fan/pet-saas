<template>
  <div class="group-activity-list">
    <el-card>
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="活动标题">
          <el-input v-model="queryForm.title" placeholder="请输入活动标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option :value="0" label="未开始" />
            <el-option :value="1" label="进行中" />
            <el-option :value="2" label="已结束" />
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
          新建拼团活动
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="活动标题" />
        <el-table-column prop="goodsName" label="商品" />
        <el-table-column prop="skuSpec" label="规格" />
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }"><span style="text-decoration: line-through">¥{{ row.originalPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="groupPrice" label="拼团价" width="100">
          <template #default="{ row }"><span style="color: #f56c6c; font-weight: bold">¥{{ row.groupPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="groupSize" label="成团人数" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="soldCount" label="已售" width="80" />
        <el-table-column label="活动时间" width="320">
          <template #default="{ row }">
            <div>{{ row.startTime }}</div>
            <div style="color: #999">至</div>
            <div>{{ row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">查看详情</el-button>
            <el-button type="success" link @click="handleViewGroups(row)">查看拼团</el-button>
            <el-button type="info" link @click="handleGeneratePoster(row)">生成海报</el-button>
          </template>
        </el-table-column>
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
import { ElMessage, type FormInstance } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getGroupActivityList, type GroupActivity, type GroupActivityQuery, type GroupActivityStatus } from '@/api/groupActivity'

const router = useRouter()
const loading = ref(false)
const tableData = ref<GroupActivity[]>([])
const total = ref(0)

const queryForm = reactive<GroupActivityQuery>({
  page: 1,
  pageSize: 10,
  title: '',
  status: undefined
})

function getStatusType(status: GroupActivityStatus) {
  const map: Record<GroupActivityStatus, any> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status: GroupActivityStatus) {
  const map: Record<GroupActivityStatus, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getGroupActivityList(queryForm)
    tableData.value = res.records
    total.value = res.total
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.page = 1
  queryForm.title = ''
  queryForm.status = undefined
  loadData()
}

function handleCreate() {
  router.push('/shop/group-activity/create')
}

function handleDetail(row: GroupActivity) {
  router.push(`/shop/group-activity/detail/${row.id}`)
}

function handleViewGroups(row: GroupActivity) {
  router.push(`/shop/group-activity/${row.id}/groups`)
}

function handleGeneratePoster(row: GroupActivity) {
  ElMessage.info('海报功能待实现')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.group-activity-list {
  .search-form {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
