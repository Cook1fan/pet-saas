<template>
  <view class="member-center-container">
    <!-- 顶部用户信息 -->
    <view class="header-section">
      <view class="user-info">
        <image class="user-avatar" :src="memberInfo?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="user-details">
          <view class="user-name">{{ memberInfo?.nickname || '宠物主人' }}</view>
          <view class="user-phone">{{ memberInfo?.phone || '' }}</view>
        </view>
      </view>
      <view class="settings-btn" @tap="navigateToSettings">⚙️</view>
    </view>

    <!-- 当前门店信息 -->
    <view class="shop-bar" @tap="navigateToShopManage">
      <view class="shop-info">
        <text class="shop-label">当前门店</text>
        <text class="shop-name">{{ currentShop?.shopName || '请选择门店' }}</text>
      </view>
      <text class="shop-arrow">›</text>
    </view>

    <!-- 统计数据 -->
    <view class="stats-section">
      <view class="stat-item" @tap="navigateToRecharge">
        <text class="stat-value">¥{{ memberStats.balance }}</text>
        <text class="stat-label">余额</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item" @tap="navigateToCards">
        <text class="stat-value">{{ memberStats.cardCount }}</text>
        <text class="stat-label">次卡</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item" @tap="navigateToOrders">
        <text class="stat-value">{{ memberStats.orderCount }}</text>
        <text class="stat-label">订单</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ memberStats.points }}</text>
        <text class="stat-label">积分</text>
      </view>
    </view>

    <!-- 快捷功能入口 -->
    <view class="quick-nav-section">
      <view class="quick-nav-grid">
        <view class="quick-nav-item" @tap="navigateToPets">
          <text class="nav-icon">🐱</text>
          <text class="nav-label">我的宠物</text>
        </view>
        <view class="quick-nav-item" @tap="navigateToRecharge">
          <text class="nav-icon">💳</text>
          <text class="nav-label">储值充值</text>
        </view>
        <view class="quick-nav-item" @tap="navigateToCards">
          <text class="nav-icon">🎫</text>
          <text class="nav-label">我的次卡</text>
        </view>
        <view class="quick-nav-item" @tap="navigateToVerifyCode">
          <text class="nav-icon">📇</text>
          <text class="nav-label">核销码</text>
        </view>
      </view>
    </view>

    <!-- 服务提醒 -->
    <view class="reminder-section" v-if="reminders.length > 0">
      <view class="section-header">
        <text class="section-title">🔔 服务提醒</text>
      </view>
      <view class="reminder-list">
        <view class="reminder-item" v-for="reminder in reminders" :key="reminder.id">
          <view class="reminder-content">
            <text class="reminder-pet">{{ reminder.petName }}</text>
            <text class="reminder-text">{{ reminder.content }}</text>
          </view>
          <view class="reminder-date">{{ reminder.date }}</view>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-item" @tap="navigateToOrders">
        <text class="menu-icon">📋</text>
        <text class="menu-label">我的订单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="navigateToChat">
        <text class="menu-icon">💬</text>
        <text class="menu-label">智能客服</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="navigateToShopManage">
        <text class="menu-icon">📍</text>
        <text class="menu-label">门店管理</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="navigateToAbout">
        <text class="menu-icon">ℹ️</text>
        <text class="menu-label">关于我们</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-placeholder"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad, onShow } from 'vue'
import { useMemberStore } from '@/stores/member'
import { getMemberIndex } from '@/api/index'
import type { MemberInfo, ShopInfo, ServiceReminder } from '@/types'

const memberStore = useMemberStore()

const memberInfo = ref<MemberInfo | null>(null)
const currentShop = ref<ShopInfo | null>(null)
const memberStats = ref({
  balance: 0,
  cardCount: 0,
  orderCount: 0,
  points: 0
})
const reminders = ref<ServiceReminder[]>([])

onLoad(() => {
  checkLoginStatus()
})

onShow(() => {
  loadMemberIndex()
})

function checkLoginStatus() {
  if (!memberStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/welcome/index' })
  }
}

async function loadMemberIndex() {
  try {
    const res = await getMemberIndex()
    if (res.code === 200 && res.data) {
      memberInfo.value = res.data.memberInfo
      currentShop.value = res.data.currentShop
      memberStats.value = res.data.statistics
      reminders.value = res.data.reminders

      memberStore.setMemberInfo(res.data.memberInfo)
      memberStore.setCurrentShop(res.data.currentShop)
    }
  } catch (error) {
    console.error('Load member index failed:', error)
  }
}

function navigateToSettings() {
  uni.navigateTo({ url: '/pages/member/settings' })
}

function navigateToPets() {
  uni.navigateTo({ url: '/pages/member/pets' })
}

function navigateToRecharge() {
  uni.navigateTo({ url: '/pages/member/recharge' })
}

function navigateToCards() {
  uni.navigateTo({ url: '/pages/member/cards' })
}

function navigateToVerifyCode() {
  uni.navigateTo({ url: '/pages/member/verify-code' })
}

function navigateToOrders() {
  uni.navigateTo({ url: '/pages/order/list' })
}

function navigateToChat() {
  uni.navigateTo({ url: '/pages/service/ai-chat' })
}

function navigateToShopManage() {
  uni.navigateTo({ url: '/pages/shop/manage' })
}

function navigateToAbout() {
  uni.showToast({ title: '开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.member-center-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header-section {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  padding: 40px 20px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.user-phone {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.settings-btn {
  font-size: 24px;
}

.shop-bar {
  background: #fff;
  margin: 16px;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shop-label {
  font-size: 13px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.shop-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.shop-arrow {
  font-size: 18px;
  color: #ccc;
}

.stats-section {
  background: #fff;
  margin: 0 16px 16px;
  padding: 20px 0;
  border-radius: 12px;
  display: flex;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #ff4d4f;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-divider {
  width: 1px;
  background: #f0f0f0;
}

.quick-nav-section {
  background: #fff;
  margin: 0 16px 16px;
  padding: 20px 16px;
  border-radius: 12px;
}

.quick-nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.nav-icon {
  font-size: 32px;
}

.nav-label {
  font-size: 12px;
  color: #666;
}

.reminder-section,
.menu-section {
  background: #fff;
  margin: 0 16px 16px;
  padding: 16px;
  border-radius: 12px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reminder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fff0f0;
  border-radius: 8px;
}

.reminder-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reminder-pet {
  font-size: 14px;
  font-weight: 600;
  color: #ff4d4f;
}

.reminder-text {
  font-size: 13px;
  color: #666;
}

.reminder-date {
  font-size: 12px;
  color: #999;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
}

.menu-label {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.menu-arrow {
  font-size: 18px;
  color: #ccc;
}

.bottom-placeholder {
  height: 20px;
}
</style>
