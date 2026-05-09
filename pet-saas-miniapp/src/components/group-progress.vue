<template>
  <view class="group-progress">
    <view class="progress-header">
      <text class="progress-title">拼团进度</text>
      <text class="progress-text">{{ currentNum }}/{{ targetNum }}人</text>
    </view>

    <view class="member-list">
      <view
        v-for="(member, index) in members"
        :key="index"
        class="member-item"
      >
        <image
          :src="member.memberAvatar || '/static/images/default-avatar.png'"
          class="member-avatar"
          mode="aspectFill"
        />
        <text class="member-name">{{ member.memberName }}</text>
        <text v-if="member.isLeader" class="leader-tag">团长</text>
      </view>

      <view
        v-for="(missing, index) in membersMissing"
        :key="'missing-' + index"
        class="member-item missing"
      >
        <view class="missing-avatar">
          <text class="missing-text">虚位以待</text>
        </view>
      </view>
    </view>

    <view class="progress-bar">
      <view
        class="progress-fill"
        :style="{ width: `${(currentNum / targetNum) * 100}%` }"
      ></view>
    </view>

    <view class="remaining">
      <text class="remaining-text">还差 {{ targetNum - currentNum }} 人成团</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  members: any[]
  currentNum: number
  targetNum: number
  membersMissing?: any[]
}

const props = defineProps<Props>()

const membersMissing = computed(() => {
  const missingCount = props.targetNum - props.currentNum
  return Array.from({ length: missingCount }, (_, i) => ({ index: i }))
})
</script>

<style scoped lang="scss">
.group-progress {
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .progress-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .progress-text {
    font-size: 28rpx;
    color: #666;
  }
}

.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 20rpx;

  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;

    .member-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      background: #f5f5f5;
    }

    .member-name {
      font-size: 24rpx;
      color: #666;
    }

    .leader-tag {
      font-size: 20rpx;
      color: #fff;
      background: #ff4d4f;
      padding: 2rpx 8rpx;
      border-radius: 4rpx;
    }
  }

  .missing {
    .missing-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      background: #f5f5f5;
      display: flex;
      justify-content: center;
      align-items: center;

      .missing-text {
        font-size: 18rpx;
        color: #999;
      }
    }
  }
}

.progress-bar {
  width: 100%;
  height: 8rpx;
  background: #f5f5f5;
  border-radius: 4rpx;
  overflow: hidden;
  margin-bottom: 16rpx;

  .progress-fill {
    height: 100%;
    background: #52c41a;
    border-radius: 4rpx;
    transition: width 0.3s ease;
  }
}

.remaining {
  text-align: center;

  .remaining-text {
    font-size: 28rpx;
    color: #ff4d4f;
    font-weight: 500;
  }
}
</style>
