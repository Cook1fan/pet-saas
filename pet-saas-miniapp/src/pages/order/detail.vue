<template>
  <view class="order-detail-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">订单详情</view>
    </view>

    <!-- 订单状态区域 -->
    <view class="status-section" v-if="orderInfo">
      <text class="status-icon">{{ getStatusIcon() }}</text>
      <text class="status-text">{{ orderInfo.statusText }}</text>
      <text class="status-desc" v-if="orderInfo.status === 'grouping'">还差1人成团，邀请好友加入吧</text>
      <text class="status-desc" v-else-if="orderInfo.status === 'success'">请向门店出示核销码使用服务</text>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-section" v-if="orderInfo">
      <!-- 商品信息 -->
      <view class="info-block">
        <view class="block-title">商品信息</view>
        <view class="product-item">
          <image class="product-cover" :src="orderInfo.coverImage || '/static/order-placeholder.png'" mode="aspectFill" />
          <view class="product-info">
            <view class="product-name">{{ orderInfo.title }}</view>
            <view class="product-spec" v-if="orderInfo.spec">规格: {{ orderInfo.spec }}</view>
          </view>
          <view class="product-price">¥{{ orderInfo.payAmount }}</view>
        </view>
      </view>

      <!-- 使用宠物 -->
      <view class="info-block" v-if="orderInfo.petName">
        <view class="block-title">使用宠物</view>
        <view class="pet-row">
          <text class="pet-name">{{ orderInfo.petName }}</text>
        </view>
      </view>

      <!-- 核销码 -->
      <view class="info-block" v-if="orderInfo.status === 'success'">
        <view class="block-title">核销码</view>
        <view class="verify-code-card" @tap="navigateToVerifyCode">
          <view class="code-display">
            <text class="code-text">{{ orderInfo.verifyCode || '8888 8888' }}</text>
            <text class="code-tip">点击查看完整二维码</text>
          </view>
          <view class="code-arrow">›</view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="info-block">
        <view class="block-title">订单信息</view>
        <view class="info-row">
          <text class="info-label">订单编号</text>
          <text class="info-value">{{ orderInfo.orderNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ formatTime(orderInfo.createTime) }}</text>
        </view>
        <view class="info-row" v-if="orderInfo.payTime">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ formatTime(orderInfo.payTime) }}</text>
        </view>
      </view>

      <!-- 支付信息 -->
      <view class="info-block">
        <view class="block-title">支付信息</view>
        <view class="info-row">
          <text class="info-label">商品金额</text>
          <text class="info-value">¥{{ orderInfo.payAmount }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">优惠金额</text>
          <text class="info-value">-¥0</text>
        </view>
        <view class="info-row total-row">
          <text class="info-label">实付金额</text>
          <text class="info-value price">¥{{ orderInfo.payAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="orderInfo">
      <view class="bottom-left">
        <view class="bottom-btn" @tap="contactService">联系客服</view>
      </view>
      <view class="bottom-right">
        <view class="bottom-btn secondary" v-if="orderInfo.status === 'pending'" @tap="handleCancel">取消订单</view>
        <view class="bottom-btn primary" v-if="orderInfo.status === 'pending'" @tap="handlePay">去支付</view>
        <view class="bottom-btn primary" v-if="orderInfo.status === 'grouping'" @tap="handleInvite">邀请好友</view>
        <view class="bottom-btn primary" v-if="orderInfo.status === 'success'" @tap="navigateToVerifyCode">查看核销码</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad } from 'vue'
import { getOrderDetail } from '@/api/index'
import type { OrderInfo } from '@/types'

const orderId = ref('')
const orderInfo = ref<OrderInfo | null>(null)

onLoad((options) => {
  if (options.orderId) {
    orderId.value = options.orderId
    loadOrderDetail()
  }
})

async function loadOrderDetail() {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await getOrderDetail(orderId.value)
    if (res.code === 200 && res.data) {
      orderInfo.value = res.data
    }
  } catch (error) {
    console.error('Load order detail failed:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

function getStatusIcon() {
  const iconMap: Record<string, string> = {
    pending: '⏳',
    grouping: '🎊',
    success: '✅',
    used: '✅',
    refunded: '💸'
  }
  return iconMap[orderInfo.value?.status || ''] || '📋'
}

function goBack() {
  uni.navigateBack()
}

function navigateToVerifyCode() {
  uni.navigateTo({ url: '/pages/member/verify-code' })
}

function contactService() {
  uni.navigateTo({ url: '/pages/service/ai-chat' })
}

function handleCancel() {
  uni.showModal({
    title: '提示',
    content: '确定要取消订单吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '取消成功', icon: 'success' })
        setTimeout(() => {
          goBack()
        }, 1000)
      }
    }
  })
}

function handlePay() {
  uni.showToast({ title: '跳转支付', icon: 'none' })
}

function handleInvite() {
  uni.showShareMenu({ withShareTicket: true })
}

function formatTime(timeStr: string) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}
</script>

<style lang="scss" scoped>
.order-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
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

.status-section {
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.status-icon {
  font-size: 48px;
}

.status-text {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.status-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.order-info-section {
  margin-top: 12px;
}

.info-block {
  background: #fff;
  padding: 16px;
  margin-bottom: 12px;
}

.block-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.product-item {
  display: flex;
  gap: 12px;
}

.product-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #ff4d4f;
}

.pet-row {
  padding: 8px 0;
}

.pet-name {
  font-size: 14px;
  color: #333;
}

.verify-code-card {
  background: #fff0f0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.code-display {
  flex: 1;
}

.code-text {
  font-size: 20px;
  font-weight: 600;
  color: #ff4d4f;
  letter-spacing: 4px;
}

.code-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.code-arrow {
  font-size: 24px;
  color: #ccc;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.info-label {
  font-size: 13px;
  color: #666;
}

.info-value {
  font-size: 13px;
  color: #333;

  &.price {
    font-size: 18px;
    font-weight: 700;
    color: #ff4d4f;
  }
}

.total-row {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
  padding-bottom: calc(12px + constant(safe-area-inset-bottom));
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}

.bottom-left {
  flex: 1;
}

.bottom-right {
  display: flex;
  gap: 12px;
}

.bottom-btn {
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;

  &.secondary {
    background: #f5f5f5;
    color: #666;
  }

  &.primary {
    background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
    color: #fff;
  }
}
</style>
