<template>
  <div class="group-activity-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>拼团活动详情</span>
        </div>
      </template>

      <div v-loading="loading">
        <div v-if="activity" class="detail-content">
          <!-- 活动基本信息 -->
          <el-descriptions :column="2" border style="margin-bottom: 24px">
            <el-descriptions-item label="活动标题">{{ activity.title }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(activity.status)">{{ getStatusText(activity.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="商品名称">{{ activity.goodsName }}</el-descriptions-item>
            <el-descriptions-item label="商品规格">{{ activity.skuSpec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="原价">
              <span style="text-decoration: line-through">¥{{ activity.originalPrice }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="拼团价">
              <span style="color: #f56c6c; font-weight: bold; font-size: 18px">¥{{ activity.groupPrice }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="成团人数">{{ activity.groupSize }}人</el-descriptions-item>
            <el-descriptions-item label="拼团有效期">{{ activity.expireHours }}小时</el-descriptions-item>
            <el-descriptions-item label="活动库存">{{ activity.stock }}</el-descriptions-item>
            <el-descriptions-item label="已售">{{ activity.soldCount }}</el-descriptions-item>
            <el-descriptions-item label="限购数量">每人{{ activity.limitPerUser }}件</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ activity.createTime }}</el-descriptions-item>
            <el-descriptions-item label="活动时间" :span="2">
              {{ activity.startTime }} 至 {{ activity.endTime }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 统计数据 -->
          <el-divider content-position="left">活动统计</el-divider>
          <el-row :gutter="16" style="margin-bottom: 24px">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ activity.totalOrderCount || 0 }}</div>
                <div class="stat-label">总订单数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card success">
                <div class="stat-value">{{ activity.successGroupCount || 0 }}</div>
                <div class="stat-label">成功团数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card ongoing">
                <div class="stat-value">{{ activity.ongoingGroupCount || 0 }}</div>
                <div class="stat-label">进行中团数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card failed">
                <div class="stat-value">{{ activity.failedGroupCount || 0 }}</div>
                <div class="stat-label">失败团数</div>
              </el-card>
            </el-col>
          </el-row>
          <el-row style="margin-bottom: 24px">
            <el-col :span="24">
              <el-card shadow="hover" class="stat-card gmv">
                <div class="stat-value">¥{{ (activity.totalGmv || 0).toFixed(2) }}</div>
                <div class="stat-label">总GMV</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="primary" @click="handleViewGroups">
              <el-icon><List /></el-icon>
              查看拼团列表
            </el-button>
            <el-button type="success" @click="handleGeneratePoster">
              <el-icon><Picture /></el-icon>
              生成海报
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, List, Picture } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { getGroupActivityDetail, type GroupActivity, type GroupActivityStatus } from '@/api/groupActivity'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const activity = ref<GroupActivity | null>(null)

function getStatusType(status: GroupActivityStatus) {
  const map: Record<GroupActivityStatus, any> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status: GroupActivityStatus) {
  const map: Record<GroupActivityStatus, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('活动ID不存在')
    handleBack()
    return
  }

  loading.value = true
  try {
    activity.value = await getGroupActivityDetail(id)
  } catch (e) {
    ElMessage.error('加载活动详情失败')
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push('/shop/group-activity')
}

function handleViewGroups() {
  if (activity.value) {
    router.push(`/shop/group-activity/${activity.value.id}/groups`)
  }
}

function handleGeneratePoster() {
  ElMessage.info('海报功能待实现')
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.group-activity-detail {
  .card-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .detail-content {
    .stat-card {
      text-align: center;

      &.success {
        border-left: 4px solid #67c23a;
      }

      &.ongoing {
        border-left: 4px solid #409eff;
      }

      &.failed {
        border-left: 4px solid #f56c6c;
      }

      &.gmv {
        border-left: 4px solid #e6a23c;
      }

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 8px;
      }

      .stat-label {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 16px;
    justify-content: center;
    padding-top: 24px;
  }
}
</style>
