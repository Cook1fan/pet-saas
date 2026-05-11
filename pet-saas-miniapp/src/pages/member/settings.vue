<template>
  <view class="settings-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">设置</view>
    </view>

    <!-- 设置列表 -->
    <view class="settings-list">
      <view class="settings-item" @tap="editProfile">
        <text class="item-label">编辑资料</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="settings-item" @tap="bindPhone">
        <text class="item-label">绑定手机号</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="settings-item" @tap="aboutUs">
        <text class="item-label">关于我们</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="settings-item" @tap="agreement">
        <text class="item-label">用户协议</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="settings-item" @tap="privacy">
        <text class="item-label">隐私政策</text>
        <text class="item-arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <view class="logout-btn" @tap="handleLogout">退出登录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useMemberStore } from '@/stores/member'

function goBack() {
  uni.navigateBack()
}

function editProfile() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function bindPhone() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function aboutUs() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function agreement() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function privacy() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        const memberStore = useMemberStore()
        memberStore.logout()
        uni.reLaunch({ url: '/pages/welcome/index' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.settings-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
}

.nav-back {
  font-size: 24px;
  font-weight: 600;
  margin-right: 16px;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
}

.settings-list {
  background: #fff;
  margin-top: 12px;
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.settings-item:last-child {
  border-bottom: none;
}

.item-label {
  font-size: 14px;
  color: #333;
}

.item-arrow {
  font-size: 18px;
  color: #ccc;
}

.logout-section {
  padding: 40px 20px;
}

.logout-btn {
  width: 100%;
  height: 44px;
  border: 1px solid #e8e8e8;
  border-radius: 22px;
  background: #fff;
  color: #ff4d4f;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
