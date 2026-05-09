<template>
  <view class="home">
    <!-- 未登录状态 -->
    <view class="login-tip-box" v-if="!isLoggedIn">
      <view class="login-tip-content">
        <text class="login-tip-text">登录后可查看拼团活动</text>
        <button class="login-btn" type="primary" @tap="goToLogin">立即登录</button>
      </view>
    </view>

    <!-- 已登录，有活动数据 -->
    <template v-else>
      <view class="title-box">
        <text class="page-title">宠物门店拼团</text>
      </view>
      <view class="content-box" v-if="activities.length > 0">
        <view class="item-list">
          <view
            class="item"
            v-for="activity in activities.slice(0, 3)"
            :key="activity.id"
            @tap="goToGroupList"
          >
            <view class="item-icon">
              <image
                v-if="activity.goodsImage"
                :src="activity.goodsImage"
                mode="aspectFill"
                class="goods-img"
              />
              <text v-else>{{ getEmoji(activity.goodsName) }}</text>
            </view>
            <view class="item-info">
              <text class="item-name">{{ activity.goodsName }}</text>
              <view class="item-bottom">
                <text class="item-price">¥{{ activity.price }}</text>
                <text class="item-group">{{ activity.groupCount }}人团</text>
              </view>
            </view>
          </view>
        </view>
        <view class="action-box">
          <button class="action-btn" size="default" @tap="goToGroupList">查看全部</button>
        </view>
      </view>
      <view class="empty-box" v-else-if="!loading">
        <text class="empty-text">暂无活动</text>
      </view>
      <view class="loading-box" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGroupActivityList, type UserGroupActivityListResp } from '@/api/group-activity'

const activities = ref<UserGroupActivityListResp[]>([])
const loading = ref(false)
const isLoggedIn = ref(false)

onMounted(() => {
  checkLoginAndFetch()
})

function checkLoginAndFetch() {
  const token = uni.getStorageSync('token')
  isLoggedIn.value = !!token

  if (!token) {
    return // 显示登录提示
  }

  fetchActivities()
}

async function fetchActivities() {
  loading.value = true
  try {
    const res = await getGroupActivityList()
    if (res.code === 200) {
      activities.value = res.data || []
    }
  } catch (error) {
    console.error('获取活动失败:', error)
  } finally {
    loading.value = false
  }
}

function getEmoji(goodsName: string): string {
  const name = goodsName || ''
  if (name.includes('猫')) return '🐱'
  if (name.includes('狗')) return '🐶'
  if (name.includes('零食') || name.includes('食')) return '🦴'
  if (name.includes('沐浴') || name.includes('洗')) return '🛁'
  return '🎁'
}

function goToLogin() {
  uni.navigateTo({
    url: '/pages/user/login'
  })
}

function goToGroupList() {
  uni.switchTab({
    url: '/pages/group-activity/list'
  })
}
</script>

<style lang="scss">
.home {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 40rpx 30rpx;
  box-sizing: border-box;
}

.login-tip-box {
  height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-tip-content {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 60rpx 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-tip-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 40rpx;
}

.login-btn {
  background-color: #ff4d4f;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
  width: 300rpx;
}

.title-box {
  text-align: center;
  margin-bottom: 60rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.content-box {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 40rpx;
  box-sizing: border-box;
}

.item-list {
  margin-bottom: 40rpx;
}

.item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.item:last-child {
  border-bottom: none;
}

.item-icon {
  font-size: 80rpx;
  margin-right: 24rpx;
  background-color: #fff7f7;
  width: 160rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.goods-img {
  width: 100%;
  height: 100%;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 12rpx;
}

.item-bottom {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.item-price {
  font-size: 36rpx;
  color: #ff4d4f;
  font-weight: bold;
  margin-right: 20rpx;
}

.item-group {
  font-size: 22rpx;
  color: #666;
  background-color: #fff0f0;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.action-box {
  text-align: center;
}

.action-btn {
  background-color: #ff4d4f;
  color: white;
  border-radius: 40rpx;
  font-size: 32rpx;
}

.empty-box,
.loading-box {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 100rpx 40rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-text,
.loading-text {
  font-size: 28rpx;
  color: #999;
}
</style>
