<template>
  <view class="my-orders">
    <!-- 顶部Tab -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      class="order-list"
      scroll-y
      @scrolltolower="loadMore"
      @refresherrefresh="refresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <view
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="goToOrderDetail(order.id)"
      >
        <!-- 订单头部 -->
        <view class="order-header">
          <text class="order-time">{{ formatTime(order.createTime) }}</text>
          <text class="order-status" :class="getStatusClass(order.groupStatus)">
            {{ order.groupStatusDesc }}
          </text>
        </view>

        <!-- 商品信息 -->
        <view class="goods-info">
          <image
            :src="order.goodsImage"
            class="goods-image"
            mode="aspectFill"
          />
          <view class="goods-detail">
            <text class="goods-title">{{ order.activityTitle }}</text>
            <view class="goods-meta">
              <text class="goods-price">¥{{ order.price }}</text>
              <text class="goods-num">x{{ order.num }}</text>
            </view>
          </view>
        </view>

        <!-- 拼团进度 -->
        <view v-if="order.groupStatus === 1" class="group-progress-section">
          <view class="progress-info">
            <text class="progress-text">{{ order.currentNum }}/{{ order.targetNum }}人</text>
            <text v-if="order.remainTime" class="remain-time">
              剩余 {{ formatRemainingTime(order.remainTime) }}
            </text>
          </view>
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: `${(order.currentNum / order.targetNum) * 100}%` }"
            ></view>
          </view>
        </view>

        <!-- 订单底部 -->
        <view class="order-footer">
          <view class="price-info">
            <text class="total-label">实付</text>
            <text class="total-price">¥{{ order.payAmount }}</text>
          </view>
          <view class="order-actions">
            <view
              v-if="order.groupStatus === 1"
              class="action-btn primary"
              @click.stop="handleShare(order)"
            >
              <text class="btn-text">邀请好友</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="orders.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无拼团订单</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyGroupOrders, MyGroupOrder } from '@/api/group-activity'

const tabs = [
  { label: '全部', value: undefined },
  { label: '拼团中', value: 1 },
  { label: '拼团成功', value: 2 },
  { label: '拼团失败', value: 3 }
]

const currentTab = ref<number | undefined>()
const orders = ref<MyGroupOrder[]>([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)

const fetchData = async (loadMore = false) => {
  if (!loadMore) {
    pageNum.value = 1
    hasMore.value = true
  }

  loading.value = true
  try {
    const res = await getMyGroupOrders({
      pageNum: pageNum.value,
      pageSize: 10,
      status: currentTab.value
    })

    if (loadMore) {
      orders.value = [...orders.value, ...(res.data.records || [])]
    } else {
      orders.value = res.data.records || []
    }

    hasMore.value = orders.value.length < res.data.total
  } catch (error) {
    console.error('获取拼团订单列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const handleTabChange = (value: number | undefined) => {
  currentTab.value = value
  fetchData()
}

const refresh = () => {
  refreshing.value = true
  fetchData()
}

const loadMore = () => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    fetchData(true)
  }
}

const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  return date.toLocaleString()
}

const formatRemainingTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0) {
    return `${hours}小时${minutes}分`
  }
  return `${minutes}分钟`
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    1: 'pending',
    2: 'success',
    3: 'failed'
  }
  return map[status] || ''
}

const handleShare = (order: MyGroupOrder) => {
  if (order.groupId) {
    uni.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    })
  }
}

const goToOrderDetail = (orderId: number) => {
  // 跳转到订单详情页
  console.log('跳转到订单详情:', orderId)
}

onMounted(() => {
  fetchData()
})

onShow(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.my-orders {
  min-height: 100vh;
  background: #f5f5f5;
}

.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;

    .tab-text {
      font-size: 28rpx;
      color: #666;
      position: relative;
      padding-bottom: 8rpx;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 0;
        height: 4rpx;
        background: #ff4d4f;
        border-radius: 2rpx;
        transition: width 0.3s;
      }
    }

    &.active {
      .tab-text {
        color: #ff4d4f;
        font-weight: bold;

        &::after {
          width: 48rpx;
        }
      }
    }
  }
}

.order-list {
  height: calc(100vh - 88rpx);
  padding: 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 24rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .order-time {
      font-size: 24rpx;
      color: #999;
    }

    .order-status {
      font-size: 26rpx;
      font-weight: 500;

      &.pending {
        color: #faad14;
      }

      &.success {
        color: #52c41a;
      }

      &.failed {
        color: #999;
      }
    }
  }

  .goods-info {
    display: flex;
    padding: 24rpx;
    gap: 20rpx;

    .goods-image {
      width: 160rpx;
      height: 160rpx;
      border-radius: 8rpx;
      background: #f5f5f5;
      flex-shrink: 0;
    }

    .goods-detail {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .goods-title {
        font-size: 28rpx;
        color: #333;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .goods-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .goods-price {
          font-size: 32rpx;
          color: #ff4d4f;
          font-weight: bold;
        }

        .goods-num {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }

  .group-progress-section {
    padding: 0 24rpx 24rpx;

    .progress-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .progress-text {
        font-size: 26rpx;
        color: #ff4d4f;
        font-weight: 500;
      }

      .remain-time {
        font-size: 24rpx;
        color: #999;
      }
    }

    .progress-bar {
      height: 6rpx;
      background: #f5f5f5;
      border-radius: 3rpx;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: #52c41a;
        border-radius: 3rpx;
      }
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 24rpx;
    border-top: 1rpx solid #f5f5f5;

    .price-info {
      display: flex;
      align-items: baseline;
      gap: 8rpx;

      .total-label {
        font-size: 24rpx;
        color: #666;
      }

      .total-price {
        font-size: 32rpx;
        color: #ff4d4f;
        font-weight: bold;
      }
    }

    .order-actions {
      display: flex;
      gap: 12rpx;

      .action-btn {
        padding: 10rpx 24rpx;
        border-radius: 20rpx;
        font-size: 24rpx;

        &.primary {
          background: #ff4d4f;
          color: #fff;
        }
      }
    }
  }
}

.empty-state {
  padding: 100rpx 0;
  text-align: center;

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.loading-state {
  padding: 60rpx 0;
  text-align: center;

  text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
