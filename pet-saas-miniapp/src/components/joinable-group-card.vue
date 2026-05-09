<template>
  <view class="joinable-group-card" @click="handleJoin">
    <view class="group-info">
      <view class="leader-info">
        <image
          :src="groupData.leaderAvatar || '/static/images/default-avatar.png'"
          class="leader-avatar"
          mode="aspectFill"
        />
        <text class="leader-name">{{ groupData.leaderName }}的团</text>
      </view>

      <view class="group-progress">
        <text class="progress-text">{{ groupData.currentNum }}/{{ groupData.targetNum }}人</text>
        <view class="progress-bar">
          <view
            class="progress-fill"
            :style="{ width: `${(groupData.currentNum / groupData.targetNum) * 100}%` }"
          ></view>
        </view>
      </view>

      <view class="remaining-time">
        剩余 {{ formatRemainingTime(groupData.remainTime) }}
      </view>
    </view>

    <view class="join-button">
      <text class="join-text">去参团</text>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  groupData: {
    groupId: number
    currentNum: number
    targetNum: number
    leaderAvatar: string
    leaderName: string
    remainTime: number
  }
  activityId: number
}

const props = defineProps<Props>()
const emit = defineEmits(['join'])

const handleJoin = () => {
  emit('join', {
    activityId: props.activityId,
    groupId: props.groupData.groupId
  })
}

const formatRemainingTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0) {
    return `${hours}小时${minutes}分`
  }
  return `${minutes}分钟`
}
</script>

<style scoped lang="scss">
.joinable-group-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 16rpx;

  .group-info {
    flex: 1;

    .leader-info {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 12rpx;

      .leader-avatar {
        width: 56rpx;
        height: 56rpx;
        border-radius: 50%;
        background: #f5f5f5;
      }

      .leader-name {
        font-size: 28rpx;
        color: #333;
      }
    }

    .group-progress {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 8rpx;

      .progress-text {
        font-size: 24rpx;
        color: #ff4d4f;
        font-weight: 500;
      }

      .progress-bar {
        flex: 1;
        height: 4rpx;
        background: #f5f5f5;
        border-radius: 2rpx;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: #52c41a;
          border-radius: 2rpx;
          transition: width 0.3s ease;
        }
      }
    }

    .remaining-time {
      font-size: 22rpx;
      color: #999;
    }
  }

  .join-button {
    padding: 12rpx 32rpx;
    background: #ff4d4f;
    border-radius: 24rpx;

    .join-text {
      font-size: 28rpx;
      color: #fff;
    }
  }
}
</style>
