<template>
  <view class="activity-list-container">
    <!-- 顶部 Tab -->
    <view class="tabs-section">
      <view
        class="tab-item"
        :class="{ active: currentTab === tab }"
        v-for="tab in tabs"
        :key="tab"
        @tap="switchTab(tab)"
      >
        {{ tab === 'all' ? '全部' : tab === 'group' ? '拼团' : '秒杀' }}
      </view>
    </view>

    <!-- 筛选和排序 -->
    <view class="filter-section">
      <view class="filter-item" @tap="showStatusFilter">
        <text>{{ statusText }}</text>
        <text class="filter-arrow">›</text>
      </view>
      <view class="filter-item" @tap="showSortFilter">
        <text>{{ sortText }}</text>
        <text class="filter-arrow">›</text>
      </view>
    </view>

    <!-- 活动列表 -->
    <view class="activity-list" v-if="activityList.length > 0">
      <view
        class="activity-card"
        v-for="activity in activityList"
        :key="activity.id"
        @tap="navigateToDetail(activity.id)"
      >
        <image class="activity-cover" :src="activity.coverImage" mode="aspectFill" />
        <view class="activity-info">
          <view class="activity-tag" :class="activity.type">{{ activity.type === 'group' ? '拼团' : '秒杀' }}</view>
          <view class="activity-title">{{ activity.title }}</view>
          <view class="activity-price">
            <text class="price">¥{{ activity.activityPrice }}</text>
            <text class="original-price">¥{{ activity.originalPrice }}</text>
          </view>
          <view class="activity-meta">
            <text class="stock-text">仅剩 {{ activity.stock }} 份</text>
            <text class="sold-text">已售 {{ activity.soldCount }}</text>
          </view>
          <!-- 拼团进度 -->
          <view class="group-progress" v-if="activity.type === 'group' && activity.groupInfo">
            <view class="progress-bar">
              <view
                class="progress-fill"
                :style="{
                  width: `${(activity.groupInfo.currentCount / activity.groupInfo.targetCount) * 100}%`
                }"
              ></view>
            </view>
            <view class="progress-text">
              {{ activity.groupInfo.currentCount }}/{{ activity.groupInfo.targetCount }}人
            </view>
          </view>
          <!-- 秒杀倒计时 -->
          <view class="flash-countdown" v-if="activity.type === 'flash' && activity.flashInfo">
            倒计时: {{ formatCountdown(activity.flashInfo.remainingSeconds) }}
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="!loading && activityList.length === 0">
      <text class="empty-icon">🎁</text>
      <text class="empty-text">暂无活动</text>
    </view>

    <!-- 加载中 -->
    <view class="loading-state" v-if="loading">
      <text>加载中...</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="!loading && hasMore && activityList.length > 0">
      <text>上拉加载更多</text>
    </view>
    <view class="no-more" v-if="!loading && !hasMore && activityList.length > 0">
      <text>没有更多了</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad, onShow } from 'vue'
import { getActivityList } from '@/api/index'
import type { ActivityItem } from '@/types'

const tabs = ['all', 'group', 'flash'] as const
const currentTab = ref<typeof tabs[number]>('all')
const currentStatus = ref('all')
const currentSort = ref('default')

const activityList = ref<ActivityItem[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

const statusText = computed(() => {
  const statusMap: Record<string, string> = {
    all: '全部状态',
    ongoing: '进行中',
    upcoming: '即将开始'
  }
  return statusMap[currentStatus.value] || '全部状态'
})

const sortText = computed(() => {
  const sortMap: Record<string, string> = {
    default: '默认排序',
    priceAsc: '价格从低到高',
    priceDesc: '价格从高到低',
    newFirst: '最新发布'
  }
  return sortMap[currentSort.value] || '默认排序'
})

onLoad((options) => {
  if (options.type && tabs.includes(options.type as any)) {
    currentTab.value = options.type as any
  }
  loadActivityList(true)
})

onShow(() => {
  // 每次显示时刷新
})

async function loadActivityList(refresh = false) {
  if (refresh) {
    page.value = 1
    hasMore.value = true
  }

  if (!hasMore.value && !refresh) return
  if (loading.value) return

  try {
    loading.value = true
    const res = await getActivityList({
      type: currentTab.value,
      status: currentStatus.value === 'all' ? undefined : (currentStatus.value as any),
      page: page.value,
      pageSize
    })

    if (res.code === 200 && res.data) {
      if (refresh) {
        activityList.value = res.data.list
      } else {
        activityList.value = [...activityList.value, ...res.data.list]
      }

      hasMore.value = activityList.value < res.data.total
      page.value++
    }
  } catch (error) {
    console.error('Load activity list failed:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(tab: typeof tabs[number]) {
  currentTab.value = tab
  loadActivityList(true)
}

function showStatusFilter() {
  uni.showActionSheet({
    itemList: ['全部状态', '进行中', '即将开始'],
    success: (res) => {
      const statusValues = ['all', 'ongoing', 'upcoming']
      currentStatus.value = statusValues[res.tapIndex]
      loadActivityList(true)
    }
  })
}

function showSortFilter() {
  uni.showActionSheet({
    itemList: ['默认排序', '价格从低到高', '价格从高到低', '最新发布'],
    success: (res) => {
      const sortValues = ['default', 'priceAsc', 'priceDesc', 'newFirst']
      currentSort.value = sortValues[res.tapIndex]
      loadActivityList(true)
    }
  })
}

function navigateToDetail(id: number) {
  uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
}

function onReachBottom() {
  loadActivityList(false)
}

function onPullDownRefresh() {
  loadActivityList(true)
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
}

function formatCountdown(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.activity-list-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.tabs-section {
  display: flex;
  background: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16px 0;
  font-size: 15px;
  color: #666;
  position: relative;

  &.active {
    color: #ff4d4f;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24px;
      height: 3px;
      background: #ff4d4f;
      border-radius: 2px;
    }
  }
}

.filter-section {
  display: flex;
  background: #fff;
  padding: 12px 16px;
  gap: 16px;
  margin-bottom: 12px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.filter-arrow {
  font-size: 16px;
  transform: rotate(90deg);
}

.activity-list {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
}

.activity-cover {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  margin-bottom: 6px;
  width: fit-content;

  &.group {
    background: #fff0f0;
    color: #ff4d4f;
  }

  &.flash {
    background: #fff7e6;
    color: #fa8c16;
  }
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 6px;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.activity-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
  margin-bottom: 6px;
}

.group-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #ff4d4f;
  border-radius: 3px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 11px;
  color: #ff4d4f;
  white-space: nowrap;
}

.flash-countdown {
  font-size: 11px;
  color: #ff4d4f;
  margin-top: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 100px;
  gap: 16px;
}

.empty-icon {
  font-size: 60px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding-top: 100px;
  font-size: 14px;
  color: #999;
}

.load-more,
.no-more {
  text-align: center;
  padding: 20px 0;
  font-size: 13px;
  color: #999;
}
</style>
