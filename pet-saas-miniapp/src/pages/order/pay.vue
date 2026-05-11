<template>
  <view class="pay-result-container">
    <!-- 状态图标和文案 -->
    <view class="status-section" v-if="status === 'success'">
      <text class="status-icon success">✓</text>
      <text class="status-title">支付成功</text>
      <text class="status-desc">您的拼团订单已提交</text>
    </view>
    <view class="status-section" v-else>
      <text class="status-icon fail">✕</text>
      <text class="status-title">支付失败</text>
      <text class="status-desc">请重试或联系客服</text>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-section" v-if="orderInfo">
      <view class="info-row">
        <text class="info-label">订单编号</text>
        <text class="info-value">{{ orderInfo.orderNo }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">支付金额</text>
        <text class="info-value amount">¥{{ orderInfo.payAmount }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">下单时间</text>
        <text class="info-value">{{ formatDate(orderInfo.createTime) }}</text>
      </view>
    </view>

    <!-- 操作按钮（成功状态） -->
    <view class="actions-section" v-if="status === 'success'">
      <view class="action-row">
        <view class="action-btn secondary" @tap="navigateToHome">
          返回首页
        </view>
        <view class="action-btn primary" @tap="navigateToOrderDetail">
          查看订单
        </view>
      </view>
      <view class="action-row" v-if="orderInfo?.status === 'grouping'">
        <view class="action-btn full invite" @tap="handleInvite">
          邀请好友拼团
        </view>
      </view>
    </view>

    <!-- 操作按钮（失败状态） -->
    <view class="actions-section" v-else>
      <view class="action-row">
        <view class="action-btn secondary" @tap="navigateToHome">
          返回首页
        </view>
        <view class="action-btn primary" @tap="handleRetryPay">
          重新支付
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onLoad } from 'vue'
import { getOrderDetail } from '@/api/index'
import type { OrderInfo } from '@/types'

const orderId = ref('')
const status = ref<'success' | 'fail'>('success')
const orderInfo = ref<OrderInfo | null>(null)

onLoad((options) => {
  if (options.orderId) orderId.value = options.orderId
  if (options.status) status.value = options.status as any

  if (orderId.value) {
    loadOrderDetail()
  }
})

async function loadOrderDetail() {
  try {
    const res = await getOrderDetail(orderId.value)
    if (res.code === 200 && res.data) {
      orderInfo.value = res.data
    }
  } catch (error) {
    console.error('Load order detail failed:', error)
  }
}

function navigateToHome() {
  uni.switchTab({ url: '/pages/home/index' })
}

function navigateToOrderDetail() {
  uni.redirectTo({ url: `/pages/order/detail?orderId=${orderId.value}` })
}

function handleRetryPay() {
  uni.navigateBack()
}

function handleInvite() {
  uni.showShareMenu({ withShareTicket: true })
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}
</script>

<style lang="scss" scoped>
.pay-result-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.status-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
}

.status-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin-bottom: 16px;

  &.success {
    background: #d4f7d4;
    color: #52c41a;
  }

  &.fail {
    background: #ffd4d4;
    color: #ff4d4f;
  }
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.status-desc {
  font-size: 14px;
  color: #999;
}

.order-info-section {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 32px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;

  &.amount {
    font-size: 18px;
    font-weight: 600;
    color: #ff4d4f;
  }
}

.actions-section {
  width: 100%;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.action-btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;

  &.secondary {
    background: #fff;
    color: #333;
    border: 1px solid #e8e8e8;
  }

  &.primary {
    background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
    color: #fff;
  }

  &.invite {
    background: linear-gradient(135deg, #fa8c16, #ffc53d);
    color: #fff;
  }

  &.full {
    flex: none;
    width: 100%;
  }
}
</style>
