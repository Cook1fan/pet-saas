<template>
  <view class="user-page">
    <!-- 未登录状态 -->
    <view class="user-header login-header" v-if="!isLoggedIn">
      <view class="login-prompt" @tap="goToLogin">
        <view class="user-avatar">
          <text class="avatar-text">?</text>
        </view>
        <view class="login-text-box">
          <text class="login-text">点击登录</text>
          <text class="login-tip">登录后查看更多功能</text>
        </view>
      </view>
    </view>

    <!-- 已登录状态 -->
    <view class="user-header" v-else>
      <view class="user-avatar">
        <image
          v-if="member?.avatar"
          :src="member.avatar"
          class="avatar-img"
          mode="aspectFill"
        />
        <text v-else class="avatar-text">{{ member?.nickname?.charAt(0) || '用' }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ member?.nickname || '用户' }}</text>
        <text class="user-phone">{{ member?.phone || '' }}</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @tap="goToMyOrders">
        <text class="menu-icon">📦</text>
        <text class="menu-text">我的拼团</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">🎁</text>
        <text class="menu-text">优惠券</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="handleLogout" v-if="isLoggedIn">
        <text class="menu-icon">🚪</text>
        <text class="menu-text logout-text">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { logout } from '@/api/user'

interface MemberInfo {
  id: number
  nickname?: string
  avatar?: string
  phone?: string
}

const isLoggedIn = ref(false)
const member = ref<MemberInfo | null>(null)

onMounted(() => {
  checkLoginStatus()
})

function checkLoginStatus() {
  const token = uni.getStorageSync('token')
  const memberData = uni.getStorageSync('member')

  isLoggedIn.value = !!token
  if (memberData) {
    member.value = memberData
  }
}

function goToLogin() {
  uni.navigateTo({
    url: '/pages/user/login'
  })
}

function goToMyOrders() {
  if (!isLoggedIn.value) {
    goToLogin()
    return
  }
  uni.navigateTo({
    url: '/pages/group-activity/my-orders'
  })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await logout()
        } catch (e) {
          // 忽略错误
        }
        uni.removeStorageSync('token')
        uni.removeStorageSync('member')
        isLoggedIn.value = false
        member.value = null
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style lang="scss">
.user-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.user-header {
  padding: 60rpx 30rpx;
  background-color: #ff4d4f;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.login-header {
  justify-content: center;
}

.login-prompt {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
}

.login-text-box {
  display: flex;
  flex-direction: column;
}

.login-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.login-tip {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.user-phone {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.menu-list {
  margin: 30rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333333;
}

.logout-text {
  color: #999;
}

.menu-arrow {
  font-size: 32rpx;
  color: #ccc;
}
</style>
