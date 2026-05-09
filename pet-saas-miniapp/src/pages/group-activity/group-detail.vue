<template>
  <view class="group-detail">
    <!-- 顶部活动信息 -->
    <view class="top-info">
      <image
        :src="groupDetail.goodsImage"
        class="top-image"
        mode="aspectFill"
      />
      <view class="top-overlay">
        <view class="top-info-content">
          <text class="top-title">{{ groupDetail.title }}</text>
          <view class="top-price">
            <text class="group-price">¥{{ groupDetail.price }}</text>
            <text class="origin-price">¥{{ groupDetail.originPrice }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 拼团进度 -->
    <view class="group-progress-section">
      <group-progress
        :members="groupDetail.members"
        :current-num="groupDetail.currentNum"
        :target-num="groupDetail.targetNum"
      />

      <!-- 倒计时 -->
      <view class="countdown-section">
        <text class="countdown-label">拼团倒计时</text>
        <countdown
          :remain-time="groupDetail.remainTime"
          @time-up="handleTimeUp"
        />
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-action-bar">
      <view class="action-left">
        <view class="action-btn secondary" @click="handleGoHome">
          <text class="btn-text">去首页逛逛</text>
        </view>
      </view>
      <view class="action-right">
        <view
          class="action-btn primary"
          :class="{ joined: hasJoined }"
          @click="handleAction"
        >
          <text class="btn-text">{{ hasJoined ? '分享给好友' : '我也要参团' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getGroupDetail, GroupDetail } from '@/api/group-activity'
import GroupProgress from '@/components/group-progress.vue'
import Countdown from '@/components/countdown.vue'

const groupId = ref(0)

onLoad((options) => {
  if (options.groupId) {
    groupId.value = Number(options.groupId)
    fetchData()
  }
})

const groupDetail = ref<GroupDetail>({
  groupId: 0,
  groupNo: '',
  activityId: 0,
  title: '',
  goodsImage: '',
  goodsName: '',
  price: 0,
  originPrice: 0,
  currentNum: 0,
  targetNum: 0,
  status: 1,
  expireTime: '',
  remainTime: 0,
  remainTimeDesc: '',
  canJoin: true,
  members: [],
  membersMissing: []
})

const hasJoined = ref(false)

const fetchData = async () => {
  try {
    const res = await getGroupDetail(groupId.value)
    groupDetail.value = res.data

    // 检查是否已加入拼团
    // 这里可以根据用户信息检查是否已加入该团
    hasJoined.value = false
  } catch (error) {
    console.error('获取拼团组详情失败:', error)
    uni.showToast({
      title: '获取拼团详情失败',
      icon: 'none'
    })
  }
}

const handleTimeUp = () => {
  uni.showToast({
    title: '拼团已结束',
    icon: 'none'
  })
}

const handleAction = () => {
  if (hasJoined.value) {
    // 分享给好友
    handleShare()
  } else {
    // 加入拼团
    handleJoinGroup()
  }
}

const handleJoinGroup = () => {
  uni.navigateTo({
    url: `/pages/group-activity/confirm?activityId=${groupDetail.value.activityId}&groupId=${groupId.value}`
  })
}

const handleShare = () => {
  // 调用微信分享功能
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

const handleGoHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}

onMounted(() => {
  fetchData()
})

// 分享配置
const shareTitle = computed(() => {
  return `${groupDetail.value.title} - ${groupDetail.value.currentNum}/${groupDetail.value.targetNum}人团`
})

const sharePath = computed(() => {
  return `/pages/group-activity/group-detail?groupId=${groupId.value}`
})

// 分享给好友
const onShareAppMessage = () => {
  return {
    title: shareTitle.value,
    path: sharePath.value,
    imageUrl: groupDetail.value.goodsImage
  }
}

// 分享到朋友圈
const onShareTimeline = () => {
  return {
    title: shareTitle.value,
    query: `groupId=${groupId.value}`,
    imageUrl: groupDetail.value.goodsImage
  }
}
</script>

<style scoped lang="scss">
.group-detail {
  min-height: 100vh;
  background: #f5f5f5;
}

.top-info {
  position: relative;
  width: 100%;
  height: 300rpx;

  .top-image {
    width: 100%;
    height: 100%;
    background: #f5f5f5;
  }

  .top-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 30rpx;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));

    .top-info-content {
      .top-title {
        display: block;
        font-size: 32rpx;
        color: #fff;
        font-weight: bold;
        margin-bottom: 16rpx;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .top-price {
        display: flex;
        align-items: baseline;
        gap: 12rpx;

        .group-price {
          font-size: 36rpx;
          color: #ffd700;
          font-weight: bold;
        }

        .origin-price {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.7);
          text-decoration: line-through;
        }
      }
    }
  }
}

.group-progress-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .countdown-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-top: 1rpx solid #f5f5f5;
    margin-top: 20rpx;

    .countdown-label {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;

  .action-left,
  .action-right {
    display: flex;
    gap: 20rpx;
  }

  .action-btn {
    padding: 16rpx 32rpx;
    border-radius: 40rpx;
    font-size: 28rpx;

    &.secondary {
      background: #f5f5f5;
      color: #666;
    }

    &.primary {
      background: #ff4d4f;
      color: #fff;

      &.joined {
        background: #52c41a;
      }
    }

    .btn-text {
      color: inherit;
    }
  }
}
</style>
