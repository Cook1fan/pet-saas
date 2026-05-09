<template>
  <view class="list-page">
    <view class="page-header">
      <text class="header-title">拼团活动</text>
    </view>
    <view class="list-content" v-if="activities.length > 0">
      <view
        class="activity"
        v-for="activity in activities"
        :key="activity.id"
        @tap="goToDetail(activity.id)"
      >
        <view class="activity-image">
          <image
            v-if="activity.goodsImage"
            :src="activity.goodsImage"
            mode="aspectFill"
            class="goods-img"
          />
          <view v-else class="activity-emoji">{{ getActivityEmoji(activity.goodsName) }}</view>
        </view>
        <view class="activity-details">
          <text class="activity-name">{{ activity.goodsName }}</text>
          <text class="activity-desc">{{ activity.title }}</text>
          <view class="activity-bottom">
            <text class="activity-price">¥{{ activity.price }}</text>
            <text class="activity-original">¥{{ activity.originPrice }}</text>
            <text class="activity-group">{{ activity.groupCount }}人团</text>
          </view>
        </view>
      </view>
    </view>
    <view class="empty-state" v-else-if="!loading">
      <text class="empty-text">暂无拼团活动</text>
    </view>
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGroupActivityList, type UserGroupActivityListResp } from '@/api/group-activity'

const activities = ref<UserGroupActivityListResp[]>([])
const loading = ref(false)

onMounted(() => {
  fetchActivities()
})

async function fetchActivities() {
  loading.value = true
  try {
    const res = await getGroupActivityList()
    if (res.code === 200) {
      activities.value = res.data || []
    } else {
      uni.showToast({
        title: res.data?.message || '获取活动列表失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    console.error('获取拼团活动列表失败:', error)
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

function getActivityEmoji(goodsName: string): string {
  const name = goodsName || ''
  if (name.includes('猫')) return '🐱'
  if (name.includes('狗')) return '🐶'
  if (name.includes('零食') || name.includes('食')) return '🦴'
  if (name.includes('沐浴') || name.includes('洗')) return '🛁'
  if (name.includes('窝') || name.includes('床')) return '🏠'
  return '🎁'
}

function goToDetail(activityId: number) {
  uni.navigateTo({
    url: `/pages/group-activity/detail?activityId=${activityId}`
  })
}
</script>

<style lang="scss">
.list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.page-header {
  background-color: #ffffff;
  padding: 30rpx;
  box-sizing: border-box;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.list-content {
  padding: 20rpx 30rpx;
  box-sizing: border-box;
}

.activity {
  display: flex;
  flex-direction: row;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.activity-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
  margin-right: 24rpx;
  flex-shrink: 0;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.goods-img {
  width: 100%;
  height: 100%;
}

.activity-emoji {
  font-size: 80rpx;
}

.activity-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.activity-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.activity-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.activity-bottom {
  display: flex;
  flex-direction: row;
  align-items: baseline;
}

.activity-price {
  font-size: 40rpx;
  color: #ff4d4f;
  font-weight: bold;
  margin-right: 16rpx;
}

.activity-original {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
  margin-right: 16rpx;
}

.activity-group {
  font-size: 22rpx;
  color: #666;
  background-color: #fff0f0;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}
</style>
