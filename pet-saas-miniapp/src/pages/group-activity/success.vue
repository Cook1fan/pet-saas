<template>
  <view class="group-success">
    <!-- 成功图标 -->
    <view class="success-icon">
      <view class="icon-circle">
        <text class="icon-check">✓</text>
      </view>
      <text class="success-text">下单成功</text>
      <text class="success-tip" v-if="!isJoinGroup">请分享给好友，完成拼团</text>
    </view>

    <!-- 订单信息 -->
    <view class="order-info">
      <view class="order-item">
        <text class="order-label">订单号</text>
        <text class="order-value">{{ orderInfo.orderNo }}</text>
      </view>
      <view class="order-item">
        <text class="order-label">商品</text>
        <text class="order-value">{{ orderInfo.goodsName }}</text>
      </view>
      <view class="order-item">
        <text class="order-label">实付金额</text>
        <text class="order-value highlight">¥{{ orderInfo.payAmount.toFixed(2) }}</text>
      </view>
    </view>

    <!-- 分享按钮（如果是开团的情况） -->
    <view v-if="!isJoinGroup" class="share-section">
      <view class="share-card">
        <text class="share-title">邀请好友参团</text>
        <text class="share-desc">还差 {{ requiredCount - currentCount }} 人成团</text>
        <view class="share-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: `${(currentCount / requiredCount) * 100}%` }"></view>
          </view>
          <text class="progress-text">{{ currentCount }}/{{ requiredCount }}人</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <view v-if="!isJoinGroup" class="action-btn primary" @click="handleShare">
        <text class="btn-text">立即分享</text>
      </view>
      <view class="action-btn secondary" @click="handleViewOrder">
        <text class="btn-text">查看订单</text>
      </view>
      <view class="action-btn secondary" @click="handleContinueShopping">
        <text class="btn-text">继续逛逛</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

const orderId = ref(0)
const groupId = ref<number>()
const isJoinGroup = ref(false)

onLoad((options) => {
  if (options.orderId) {
    orderId.value = Number(options.orderId)
  }
  if (options.groupId) {
    groupId.value = Number(options.groupId)
  }
  if (options.isJoinGroup) {
    isJoinGroup.value = options.isJoinGroup === 'true'
  }
  fetchOrderInfo()
})

const orderInfo = ref({
  orderNo: '',
  goodsName: '',
  payAmount: 0,
  goodsImage: ''
})

const currentCount = ref(1)
const requiredCount = ref(2)

const shareTitle = computed(() => {
  return `${orderInfo.value.goodsName} 2人团仅需¥${orderInfo.value.payAmount}`
})

const sharePath = computed(() => {
  return `/pages/group-activity/group-detail?groupId=${groupId.value}`
})

const fetchOrderInfo = async () => {
  try {
    // 模拟获取订单信息
    orderInfo.value = {
      orderNo: 'GP' + Date.now(),
      goodsName: '皇家猫粮 2kg',
      payAmount: 49.9,
      goodsImage: 'https://via.placeholder.com/200x200/ff6b6b/ffffff?text=Cat+Food'
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
  }
}

const handleViewOrder = () => {
  uni.redirectTo({
    url: `/pages/group-activity/my-orders`
  })
}

const handleContinueShopping = () => {
  uni.switchTab({
    url: `/pages/index/index`
  })
}

const handleShare = () => {
  // 显示分享菜单
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
  uni.showToast({
    title: '请点击右上角分享',
    icon: 'none'
  })
}

// 分享给好友
const onShareAppMessage = () => {
  return {
    title: shareTitle.value,
    path: sharePath.value,
    imageUrl: orderInfo.value.goodsImage
  }
}

// 分享到朋友圈
const onShareTimeline = () => {
  return {
    title: shareTitle.value,
    query: `groupId=${groupId.value}`,
    imageUrl: orderInfo.value.goodsImage
  }
}

onMounted(() => {
  fetchOrderInfo()
})
</script>

<style scoped lang="scss">
.group-success {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 100rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;

  .icon-circle {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #52c41a, #73d13d);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.3);

    .icon-check {
      font-size: 72rpx;
      color: #fff;
      font-weight: bold;
    }
  }

  .success-text {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 12rpx;
  }

  .success-tip {
    font-size: 26rpx;
    color: #666;
  }
}

.order-info {
  background: #fff;
  padding: 40rpx;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  min-width: 100%;

  .order-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .order-label {
      font-size: 28rpx;
      color: #666;
    }

    .order-value {
      font-size: 28rpx;
      color: #333;

      &.highlight {
        color: #ff4d4f;
        font-weight: bold;
        font-size: 32rpx;
      }
    }
  }
}

.share-section {
  width: 100%;
  margin-bottom: 30rpx;

  .share-card {
    background: linear-gradient(135deg, #fff7f7, #fff1f0);
    padding: 30rpx;
    border-radius: 16rpx;
    border: 2rpx solid #ffccc7;

    .share-title {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #ff4d4f;
      margin-bottom: 12rpx;
    }

    .share-desc {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 20rpx;
    }

    .share-progress {
      display: flex;
      align-items: center;
      gap: 20rpx;

      .progress-bar {
        flex: 1;
        height: 12rpx;
        background: #f5f5f5;
        border-radius: 6rpx;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #52c41a, #73d13d);
          border-radius: 6rpx;
          transition: width 0.3s ease;
        }
      }

      .progress-text {
        font-size: 26rpx;
        color: #ff4d4f;
        font-weight: 500;
      }
    }
  }
}

.action-buttons {
  min-width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20rpx;

  .action-btn {
    padding: 24rpx;
    border-radius: 40rpx;
    text-align: center;
    font-size: 32rpx;
    font-weight: bold;

    &.primary {
      background: linear-gradient(135deg, #ff6b6b, #ff4d4f);
      color: #fff;
      box-shadow: 0 8rpx 24rpx rgba(255, 77, 79, 0.3);
    }

    &.secondary {
      background: #f5f5f5;
      color: #666;
    }
  }
}
</style>
