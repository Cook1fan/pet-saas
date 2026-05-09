<template>
  <div class="group-buy-groups">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>拼团组列表</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="团状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option :value="0" label="拼团中" />
            <el-option :value="1" label="拼团成功" />
            <el-option :value="2" label="拼团失败" />
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
        <el-table-column prop="groupNo" label="团号" />
        <el-table-column label="团长信息" width="200">
          <template #default="{ row }">
            <div class="member-info">
              <el-avatar v-if="row.leaderAvatar" :src="row.leaderAvatar" :size="30" style="margin-right: 8px" />
              {{ row.leaderName || '匿名用户' }}
              <div style="font-size: 12px; color: #999">{{ row.leaderPhone }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="人数" width="120">
          <template #default="{ row }">
            <el-tag :type="getGroupSizeType(row.currentSize, row.targetSize)">
              {{ row.currentSize }}/{{ row.targetSize }}人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="完成时间" width="180">
          <template #default="{ row }">
            <template v-if="row.status === 1">
              {{ row.successTime }}
            </template>
            <template v-else-if="row.status === 2">
              {{ row.expireTime }} (失败)
            </template>
            <template v-else>
              {{ row.expireTime }} (过期)
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewMembers(row)">查看成员</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table-column v-if="expandedGroupId === -1" type="expand">
        <template #default="{ row }">
          <div class="member-list">
            <h4>团成员信息：</h4>
            <div v-if="row.members && row.members.length > 0" class="members">
              <div v-for="member in row.members" :key="member.id" class="member-item">
                <el-avatar v-if="member.avatar" :src="member.avatar" :size="40" />
                <div class="member-details">
                  <div class="member-name">
                    {{ member.nickname || '匿名用户' }}
                    <el-tag v-if="member.isLeader" type="danger" size="small">团长</el-tag>
                  </div>
                  <div class="member-phone">{{ member.phone }}</div>
                  <div class="join-time">加入时间：{{ member.joinTime }}</div>
                </div>
              </div>
            </div>
            <div v-else>该团暂无成员</div>
          </div>
        </template>
      </el-table-column>

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
import { ArrowLeft, Search, Refresh } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getActivityGroupList, type GroupBuyGroup, type GroupBuyGroupQuery, type GroupBuyStatus } from '@/api/groupActivity'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const tableData = ref<GroupBuyGroup[]>([])
const total = ref(0)
const expandedGroupId = ref(-1)

const queryForm = reactive<GroupBuyGroupQuery>({
  page: 1,
  pageSize: 10,
  status: undefined
})

function getStatusType(status: GroupBuyStatus) {
  const map: Record<GroupBuyStatus, any> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status: GroupBuyStatus) {
  const map: Record<GroupBuyStatus, string> = { 0: '拼团中', 1: '拼团成功', 2: '拼团失败' }
  return map[status] || '未知'
}

function getGroupSizeType(currentSize: number, targetSize: number) {
  if (currentSize >= targetSize) return 'success'
  return 'warning'
}

function handleViewMembers(row: GroupBuyGroup) {
  // 如果已经展开，则收起；否则展开
  if (expandedGroupId.value === row.id) {
    expandedGroupId.value = -1
  } else {
    expandedGroupId.value = row.id
    // 如果没有成员数据，加载成员数据
    if (!row.members) {
      loadGroupMembers(row.id)
    }
  }
}

async function loadGroupMembers(groupId: number) {
  // 这里应该调用API获取成员数据，现在使用模拟数据
  const group = tableData.value.find(item => item.id === groupId)
  if (group) {
    // 模拟成员数据
    group.members = [
      {
        id: 1,
        userId: 101,
        nickname: '张三',
        avatar: 'https://via.placeholder.com/40',
        phone: '13800138001',
        isLeader: true,
        joinTime: '2023-06-15 10:00:00'
      },
      {
        id: 2,
        userId: 102,
        nickname: '李四',
        avatar: 'https://via.placeholder.com/40',
        phone: '13800138002',
        isLeader: false,
        joinTime: '2023-06-15 10:30:00'
      },
      {
        id: 3,
        userId: 103,
        nickname: '王五',
        avatar: 'https://via.placeholder.com/40',
        phone: '13800138003',
        isLeader: false,
        joinTime: '2023-06-15 11:00:00'
      }
    ]
  }
}

async function loadData() {
  const activityId = Number(route.params.id)
  if (!activityId) {
    ElMessage.error('活动ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getActivityGroupList(activityId, queryForm)
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
  queryForm.status = undefined
  loadData()
}

function handleBack() {
  router.push(`/shop/group-activity/detail/${route.params.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.group-buy-groups {
  .card-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }

  .member-list {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #fafafa;

    h4 {
      margin-bottom: 16px;
      color: #303133;
    }

    .members {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 16px;
    }

    .member-item {
      display: flex;
      gap: 12px;
      padding: 12px;
      background: #fff;
      border-radius: 4px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);

      .member-details {
        flex: 1;

        .member-name {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: bold;
          margin-bottom: 4px;
        }

        .member-phone {
          color: #909399;
          font-size: 12px;
          margin-bottom: 2px;
        }

        .join-time {
          color: #909399;
          font-size: 12px;
        }
      }
    }
  }

  .member-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }
}
</style>
