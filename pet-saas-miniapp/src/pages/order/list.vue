<template>
  <view class="order-list-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">‹</view>
      <view class="nav-title">我的订单</view>
    </view>

    <!-- 订单状态标签 -->
    <scroll-view class="tabs-scroll" scroll-x :show-scrollbar="false">
      <view class="tabs-list">
        <view
          class="tab-item"
          :class="{ active: currentTab === tab.value }"
          v-for="tab in orderTabs"
          :key="tab.value"
          @tap="switchTab(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>
    </scroll-view>

    <!-- 订单列表 -->
    <view class="orders-list" v-if="filteredOrders.length > 0">
      <view
        class="order-card"
        v-for="order in filteredOrders"
        :key="order.orderId"
        @tap="navigateToDetail(order.orderId)"
      >
        <view class="order-header">
          <text class="order-no">订单号: {{ order.orderNo }}</text>
          <text class="order-status">{{ order.statusText }}</text>
        </view>
        <view class="order-content">
          <image class="order-cover" :src="order.coverImage || '/static/order-placeholder.png'" mode="aspectFill" />
          <view class="order-info">
            <view class="order-title">{{ order.title }}</view>
            <view class="order-count" v-if="order.count">x{{ order.count }}</view>
          </view>
          <view class="order-price">¥{{ order.payAmount }}</view>
        </view>
        <view class="order-footer">
          <text class="order-time">{{ formatTime(order.createTime) }}</text>
          <view class="order-actions">
            <view class="action-btn" v-if="order.status === 'pending'">去支付</view>
            <view class="action-btn" v-if="order.status === 'grouping'">邀请好友</view>
            <view class="action-btn secondary" v-if="order.status === 'success'">查看核销码</view>
            <view class="action-btn secondary" v-if="order.status === 'pending'">取消订单</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
      <view class="browse-btn" @tap="goHome">去逛逛</view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore && filteredOrders.length > 0">
      加载中...
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onLoad, onShow } from 'vue'
import { getOrderList } from '@/api/index'
import type { OrderInfo } from '@/types'

const orderTabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 'pending' },
  { label: '拼团中', value: 'grouping' },
  { label: '已完成', value: 'success' }
]

const currentTab = ref('all')
const ordersList = ref<OrderInfo[]>([])
const hasMore = ref(false)
const page = ref(1)

// 模拟数据
const mockOrders: OrderInfo[] = [
  {
    orderId: '1',
    orderNo: '20240510001',
    payAmount: 99,
    status: 'success',
    statusText: '已完成',
    title: '宠物SPA套餐',
    coverImage: '',
    count: 1,
    createTime: '2024-05-10T10:30:00'
  },
  {
    orderId: '2',
    orderNo: '20240508002',
    payAmount: 59,
    status: 'grouping',
    statusText: '拼团中',
    title: '限时秒杀-进口猫粮',
    coverImage: '',
    count: 1,
    createTime: '2024-05-08T14:20:00'
  },
  {
    orderId: '3',
    orderNo: '20240425003',
    payAmount: 199,
    status: 'pending',
    statusText: '待付款',
    title: '精致洗澡+美容',
    coverImage: '',
    count: 1,
    createTime: '2024-04-25T09:15:00'
  }
]

const filteredOrders = computed(() => {
  if (currentTab.value === 'all') {
    return ordersList.value
  }
  return ordersList.value.filter(order => order.status === currentTab.value)
})

onLoad(() => {})

onShow(() => {
  loadOrders(true)
})

async function loadOrders(refresh = false) {
  if (refresh) {
    page.value = 1
    ordersList.value = mockOrders
  }
}

function switchTab(tab: string) {
  currentTab.value = tab
}

function navigateToDetail(orderId: string) {
  uni.navigateTo({ url: `/pages/order/detail?orderId=${orderId}` })
}

function goHome() {
  uni.switchTab({ url: '/pages/home/index' })
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/member/index' })
  }
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
.order-list-container {
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

.tabs-scroll {
  background: #fff;
  white-space: nowrap;
  border-bottom: 1px solid #f0f0f0;
}

.tabs-list {
  display: flex;
  padding: 0 16px;
}

.tab-item {
  padding: 12px 16px;
  font-size: 14px;
  color: #666;
  position: relative;
  flex-shrink: 0;

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

.orders-list {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 600;
}

.order-content {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.order-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-title {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.order-count {
  font-size: 13px;
  color: #999;
}

.order-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 16px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-radius: 16px;
  font-size: 12px;

  &.secondary {
    background: #f5f5f5;
    color: #666;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 100px;
  gap: 12px;
}

.empty-icon {
  font-size: 60px;
}

.empty-text {
  font-size: 14px;
  color: #666;
}

.browse-btn {
  margin-top: 16px;
  padding: 10px 32px;
  background: linear-gradient(135deg, #ff4d4f, #ff6b6b);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
}

.load-more {
  text-align: center;
  padding: 20px;
  font-size: 13px;
  color: #999;
}
</style>
